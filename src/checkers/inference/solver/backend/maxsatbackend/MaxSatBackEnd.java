package checkers.inference.solver.backend.maxsatbackend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.BackEnd;
import checkers.inference.solver.backend.Translator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.StatisticRecorder;
import checkers.inference.solver.util.StatisticRecorder.StatisticKey;

/**
 * MaxSatBackEnd calls MaxSatSerializer that converts constraint into a list of
 * VecInt, then invoke Sat4j lib to solve the clauses, and decode the result.
 * 
 * @author jianchu
 *
 */
public class MaxSatBackEnd extends BackEnd<VecInt[], VecInt[], Integer> {

    protected final SlotManager slotManager;
    protected final List<VecInt> hardClauses = new LinkedList<VecInt>();
    protected final List<VecInt> softClauses = new LinkedList<VecInt>();
    protected final File CNFData = new File(new File("").getAbsolutePath() + "/cnfData");
    protected StringBuilder CNFInput = new StringBuilder();

    private long serializationStart;
    private long serializationEnd;
    protected long solvingStart;
    protected long solvingEnd;

    public MaxSatBackEnd(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Translator<VecInt[], VecInt[], Integer> realSerializer,
            Lattice lattice) {
        super(configuration, slots, constraints, qualHierarchy, processingEnvironment, realSerializer,
                lattice);
        this.slotManager = InferenceMain.getInstance().getSlotManager();

        if (shouldOutputCNF()) {
            CNFData.mkdir();
        }
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {

        Map<Integer, AnnotationMirror> result = new HashMap<>();
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());

        this.serializationStart = System.currentTimeMillis();
        // Serialization step:
        this.convertAll();
        this.serializationEnd = System.currentTimeMillis();

        //TODO: Would it be better to have a type restriction on Translator class
        // that a backend requires? E.g. for MaxSatBackend, the translator passed
        // in has to be a sub-class of MaxSatTranslator.
        //
        // The pros would be we can have more flexible way of interactions between
        // translators and backends without losing type safety.
        //
        // THe cons would be we will add one more type parameter on BackEnd class,
        // which makes BackEnd really complicate (we will have 4 type parameters on
        // a single class!).
        MaxSatTranslator maxSatTranslator = (MaxSatTranslator) realTranslator;
        for (Integer varSlotId : this.varSlotIds) {
            maxSatTranslator.generateOneHotClauses(hardClauses, varSlotId);
        }

        if (shouldOutputCNF()) {
            buildCNF();
            writeCNFInput();
        }
        // printClauses();
        configureSatSolver(solver);

        try {
            for (VecInt hardClause : hardClauses) {
                solver.addHardClause(hardClause);
            }

            this.hardClauses.clear();

            for (VecInt softclause : softClauses) {
                solver.addSoftClause(softclause);
            }

            this.solvingStart = System.currentTimeMillis();
            boolean isSatisfiable = solver.isSatisfiable();
            this.solvingEnd = System.currentTimeMillis();

            long solvingTime = solvingEnd - solvingStart;
            long serializationTime = serializationEnd - serializationStart;

            StatisticRecorder.recordSingleSerializationTime(serializationTime);
            StatisticRecorder.recordSingleSolvingTime(solvingTime);

            if (isSatisfiable) {
                result = decode(solver.model());
                // PrintUtils.printResult(result);
            } else {
                System.out.println("Not solvable!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * sat solver configuration Configure
     *
     * @param solver
     */
    private void configureSatSolver(WeightedMaxSatDecorator solver) {

        final int totalVars = (slotManager.getNumberOfSlots() * lattice.numTypes);
        final int totalClauses = hardClauses.size() + softClauses.size();

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        StatisticRecorder.record(StatisticKey.CNF_CLAUSE_SIZE, (long) totalClauses);
        countVariables();
        solver.setTimeoutMs(1000000);
    }

    /**
     * Convert constraints to list of VecInt.
     */
    @Override
    public void convertAll() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            for (VecInt res : constraint.serialize(realTranslator)) {
                if (res != null && res.size() != 0) {
                    if (constraint instanceof PreferenceConstraint) {
                        softClauses.add(res);
                    } else {
                        hardClauses.add(res);
                    }
                }
            }
        }
    }

    protected Map<Integer, AnnotationMirror> decode(int[] solution) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        for (Integer var : solution) {
            if (var > 0) {
                var = var - 1;
                int slotId = MathUtils.getSlotId(var, lattice);
                AnnotationMirror type = realTranslator.decodeSolution(var, processingEnvironment);
                result.put(slotId, type);
            }
        }
        return result;
    }

    protected void countVariables() {

        Set<Integer> vars = new HashSet<Integer>();

        for (VecInt vi : hardClauses) {
            for (int i : vi.toArray()) {
                vars.add(i);
            }
        }
        StatisticRecorder.record(StatisticKey.CNF_VARIABLE_SIZE, (long) vars.size());
    }

    protected boolean shouldOutputCNF() {
        String outputCNF = configuration.get("outputCNF");
        return outputCNF != null && outputCNF.equals("true");
    }

    /**
     * Write CNF clauses into a string.
     */
    protected void buildCNF() {

        final int totalClauses = hardClauses.size();
        final int totalVars = slotManager.getNumberOfSlots() * lattice.numTypes;

        CNFInput.append("c This is the CNF input\n");
        CNFInput.append("p cnf ");
        CNFInput.append(totalVars);
        CNFInput.append(" ");
        CNFInput.append(totalClauses);
        CNFInput.append("\n");

        for (VecInt clause : hardClauses) {
            int[] literals = clause.toArray();
            for (int i = 0; i < literals.length; i++) {
                CNFInput.append(literals[i]);
                CNFInput.append(" ");
            }
            CNFInput.append("0\n");
        }
    }

    protected void writeCNFInput() {
        writeCNFInput("cnfdata.txt");
    }

    protected void writeCNFInput(String file) {
        String writePath = CNFData.getAbsolutePath() + "/" + file;
        File f = new File(writePath);
        PrintWriter pw;
        try {
            pw = new PrintWriter(f);
            pw.write(CNFInput.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * print all soft and hard clauses for testing.
     */
    protected void printClauses() {
        System.out.println("Hard clauses: ");
        for (VecInt hardClause : hardClauses) {
            System.out.println(hardClause);
        }
        System.out.println();
        System.out.println("Soft clauses: ");
        for (VecInt softClause : softClauses) {
            System.out.println(softClause);
        }
    }
}
