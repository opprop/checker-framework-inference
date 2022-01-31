package checkers.inference.solver.backend.z3smt;

import checkers.inference.InferenceMain;
import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.model.serialization.ToStringSerializer;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.z3smt.encoder.Z3SmtSoftConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.ExternalSolverUtils;
import checkers.inference.solver.util.FileUtils;
import checkers.inference.solver.util.SolverArg;
import checkers.inference.solver.util.SolverEnvironment;
import checkers.inference.solver.util.Statistics;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.javacutil.BugInCF;

public class Z3SmtSolver<SlotEncodingT, SlotSolutionT>
        extends Solver<Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT>> {

    public enum Z3SolverEngineArg implements SolverArg {
        /** option to use optimizing mode or not */
        optimizingMode
    }

    private static final Logger logger = Logger.getLogger(Z3SmtSolver.class.getName());

    protected final Context ctx;
    protected com.microsoft.z3.Optimize solver;

    /** The StringBuilder used to serialize the z3 smt input. */
    protected StringBuilder smtFileContents;

    protected static final String z3Program = "z3";
    protected boolean optimizingMode;

    /** This field indicates that whether we are going to explain unsatisfiable.*/
    protected boolean explainUnsat;

    /**
     * This fields store the mapping from the constraint string ID to the constraint.
     * In non-optimizing mode, all ID-constraint mappings are cached during encoding,
     * so that we can retrieve the unsat constraints later using the constraint name.
     */
    protected final Map<String, Constraint> serializedConstraints = new HashMap<>();

    // file is written at projectRootFolder/constraints.smt
    // TODO: Clean up the string concatenations in here as well as the whole project
    protected static final String pathToProject = new File("").getAbsolutePath();
    protected static final String constraintsFile = pathToProject + "/z3Constraints.smt";
    protected static final String constraintsUnsatCoreFile =
            pathToProject + "/z3ConstraintsUnsatCore.smt";
    protected static final String constraintsStatsFile = pathToProject + "/z3ConstraintsGlob.smt";

    // timing statistics variables
    protected long serializationStart;
    protected long serializationEnd;
    protected long solvingStart;
    protected long solvingEnd;


    public Z3SmtSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, z3SmtFormatTranslator, lattice);

        Map<String, String> z3Args = new HashMap<>();

        // TODO: set timeout argument for z3 solver if desired
        // z3Args.put("timeout", "-1");

        // creating solver
        ctx = new Context(z3Args);

        z3SmtFormatTranslator.init(ctx);
    }

    // Main entry point
    @Override
    public Map<Integer, AnnotationMirror> solve() {
        // serialize based on user choice of running in optimizing or non-optimizing mode
        optimizingMode = solverEnvironment.getBoolArg(Z3SolverEngineArg.optimizingMode);
        explainUnsat = false;

        if (optimizingMode) {
            logger.fine("Encoding for optimizing mode");
        } else {
            logger.fine("Encoding for non-optimizing mode");
        }

        serializeSMTFileContents();

        List<String> results = new ArrayList<>();
        solvingStart = System.currentTimeMillis();
        boolean isSat = runZ3Solver(results);
        solvingEnd = System.currentTimeMillis();

        // serializationEnd and serializationStart are set within serializeSMTFileContents() above
        Statistics.addOrIncrementEntry(
                "smt_serialization_time(millisec)", serializationEnd - serializationStart);
        Statistics.addOrIncrementEntry("smt_solving_time(millisec)", solvingEnd - solvingStart);

        if (!isSat) {
            // The status is UNSAT when there's no output model
            logger.fine("!!! The set of constraints is unsatisfiable! !!!");
            return null;
        }
        
        return formatTranslator.decodeSolution(
                        results, solverEnvironment.processingEnvironment);
    }

    @Override
    public Collection<Constraint> explainUnsatisfiable() {
        optimizingMode = false;
        explainUnsat = true;

        logger.fine("Now encoding for unsat core dump.");
        serializeSMTFileContents();

        List<String> unsatConstraintIDs = new ArrayList<>();
        solvingStart = System.currentTimeMillis();
        // To explain unsat, run z3 solver a second time to get identifiers of the
        // unsatisfiable constraints
        runZ3Solver(unsatConstraintIDs);
        solvingEnd = System.currentTimeMillis();

        // serializationEnd and serializationStart are set within serializeSMTFileContents() above
        Statistics.addOrIncrementEntry(
                "smt_unsat_serialization_time(millisec)", serializationEnd - serializationStart);
        Statistics.addOrIncrementEntry(
                "smt_unsat_solving_time(millisec)", solvingEnd - solvingStart);

        List<Constraint> unsatConstraints = new ArrayList<>();

        for (String constraintID : unsatConstraintIDs) {
            Constraint c = serializedConstraints.get(constraintID);
            unsatConstraints.add(c);
        }

        return unsatConstraints;
    }

    private void serializeSMTFileContents() {
        // make a fresh solver to contain encodings of the slots
        solver = ctx.mkOptimize();
        // make a new buffer to store the serialized smt file contents
        smtFileContents = new StringBuilder();

        // only enable in non-optimizing mode
        if (!optimizingMode && explainUnsat) {
            smtFileContents.append("(set-option :produce-unsat-cores true)\n");
        }

        serializationStart = System.currentTimeMillis();
        encodeAllSlots();
        encodeAllConstraints();
        if (optimizingMode) {
            encodeAllSoftConstraints();
        }
        serializationEnd = System.currentTimeMillis();

        logger.fine("Encoding constraints done!");

        smtFileContents.append("(check-sat)\n");
        if (!optimizingMode && explainUnsat) {
            smtFileContents.append("(get-unsat-core)\n");
        } else {
            smtFileContents.append("(get-model)\n");
        }
        
        logger.fine("Writing constraints to file: " + constraintsFile);

        writeConstraintsToSMTFile();
    }

    private void writeConstraintsToSMTFile() {
        String fileContents = smtFileContents.toString();

        if (!explainUnsat) {
            // write the constraints to the file for external solver use
            FileUtils.writeFile(new File(constraintsFile), fileContents);
        } else {
            // write the unsat core constraints to the file for external solver use
            FileUtils.writeFile(new File(constraintsUnsatCoreFile), fileContents);
        }
        // write a copy in append mode to stats file for later bulk analysis
        FileUtils.appendFile(new File(constraintsStatsFile), fileContents);
    }

    protected void encodeAllSlots() {
        // preprocess slots
        formatTranslator.preAnalyzeSlots(slots);
        
        // generate slot constraints
        for (Slot slot : slots) {
            if (slot instanceof VariableSlot) {
                BoolExpr wfConstraint = formatTranslator.encodeSlotWellformednessConstraint((VariableSlot) slot);

                if (!wfConstraint.simplify().isTrue()) {
                    solver.Assert(wfConstraint);
                }
                if (optimizingMode) {
                    encodeSlotPreferenceConstraint((VariableSlot) slot);
                }
            }
        }

        // solver.toString() also includes "(check-sat)" as the last line,
        // remove it
        String slotDefinitionsAndConstraints = solver.toString();
        int truncateIndex = slotDefinitionsAndConstraints.lastIndexOf("(check-sat)");
        assert truncateIndex != -1;

        // append slot definitions to overall smt file
        smtFileContents.append(slotDefinitionsAndConstraints, 0, truncateIndex);
    }

    @Override
    protected void encodeAllConstraints() {
        int current = 1;

        StringBuilder constraintSmtFileContents = new StringBuilder();

        for (Constraint constraint : constraints) {
            BoolExpr serializedConstraint = constraint.serialize(formatTranslator);

            if (InferenceMain.isHackMode(serializedConstraint == null)) {
                // TODO: Should error abort if unsupported constraint detected.
                // Currently warning is a workaround for making ontology
                // working, as in some cases existential constraints generated.
                // Should investigate on this, and change this to ErrorAbort
                // when eliminated unsupported constraints.
                logger.fine(
                        "Unsupported constraint detected! Constraint type: "
                                + constraint.getClass().getSimpleName());
                continue;
            }

            Expr simplifiedConstraint = serializedConstraint.simplify();

            if (simplifiedConstraint.isTrue()) {
                // This only works if the BoolExpr is directly the value Z3True.
                // Still a good filter, but doesn't filter enough.
                // EG: (and (= false false) (= false false) (= 0 0) (= 0 0) (= 0 0))
                // Skip tautology.
                current++;
                continue;
            }

            if (simplifiedConstraint.isFalse()) {
                final ToStringSerializer toStringSerializer = new ToStringSerializer(false);
                throw new BugInCF(
                        "impossible constraint: "
                                + constraint.serialize(toStringSerializer)
                                + "\nSerialized:\n"
                                + serializedConstraint);
            }

            String clause = simplifiedConstraint.toString();

            if (!optimizingMode && explainUnsat) {
                // add assertions with names, for unsat core dump
                String constraintName = constraint.getClass().getSimpleName() + current;

                constraintSmtFileContents.append("(assert (! ");
                constraintSmtFileContents.append(clause);
                constraintSmtFileContents.append(" :named " + constraintName + "))\n");

                // add constraint to serialized constraints map, so that we can
                // retrieve later using the constraint name when outputting the unsat core
                serializedConstraints.put(constraintName, constraint);
            } else {
                constraintSmtFileContents.append("(assert ");
                constraintSmtFileContents.append(clause);
                constraintSmtFileContents.append(")\n");
            }

            current++;
        }

        smtFileContents.append(constraintSmtFileContents);
    }

    protected void encodeAllSoftConstraints() {
    	final Z3SmtSoftConstraintEncoder<SlotEncodingT, SlotSolutionT> encoder = formatTranslator.createSoftConstraintEncoder();
        smtFileContents.append(encoder.encodeAndGetSoftConstraints(constraints));
    }

    protected void encodeSlotPreferenceConstraint(VariableSlot varSlot) {
        // empty string means no optimization group
        // TODO: support variable weight for preference constraint
        solver.AssertSoft(
                formatTranslator.encodeSlotPreferenceConstraint(varSlot), 1, "");
    }

    /**
     * Runs z3 solver and returns the parsed results based on whether it's sat/unsat
     * @param results an output parameter that stores (1) the parsed solution if it's sat
     *                (2) the unsatisfiable constraint identifier strings otherwise
     * @return true if sat and false otherwise
     */
    private boolean runZ3Solver(List<String> results) {
        assert results != null;
        // TODO: add z3 stats?
        String[] command;
        if (!explainUnsat) {
            command = new String[] {z3Program, constraintsFile};
        } else {
            command = new String[] {z3Program, constraintsUnsatCoreFile};
        }

        // Run command
        // TODO: check that stdErr has no errors
        int exitStatus =
                ExternalSolverUtils.runExternalSolver(
                        command,
                        stdOut -> parseStdOut(stdOut, results),
                        stdErr -> ExternalSolverUtils.printStdStream(System.err, stdErr));
        // if exit status from z3 is not 0, then it is unsat
        return exitStatus == 0;
    }

    /**
     * Parses the STD output from the z3 process and handles SAT and UNSAT outputs
     * @param results For sat case, this stores the parsed solution
     *                For unsat case, this stores the unsatisfiable constraint identifiers
     *                parsed from the z3 output
     */
    private void parseStdOut(BufferedReader stdOut, List<String> results) {
        String line;

        boolean declarationLine = true;
        // each result line is "varName value"
        final StringBuilder resultsLine = new StringBuilder();

        boolean unsat = false;

        while ((line = readStdoutByLine(stdOut)) != null) {
            line = line.trim();

            if (explainUnsat) {
                // UNSAT Cases ====================
                // Parse the unsat output to get the unsatisfiable constraint identifiers
                if (line.contentEquals("unsat")) {
                    unsat = true;
                    continue;
                }
                if (unsat) {
                    if (line.startsWith("(")) {
                        line = line.substring(1); // remove open bracket
                    }
                    if (line.endsWith(")")) {
                        line = line.substring(0, line.length() - 1);
                    }

                    for (String constraintID : line.split(" ")) {
                        results.add(constraintID);
                    }
                }
            } else {
                // SAT Cases =======================
                // processing define-fun lines
                if (declarationLine && line.startsWith("(define-fun")) {
                    declarationLine = false;

                    int firstBar = line.indexOf('|');
                    int lastBar = line.lastIndexOf('|');

                    assert firstBar != -1;
                    assert lastBar != -1;
                    assert firstBar < lastBar;
                    assert line.contains("Bool") || line.contains("Int");

                    // copy z3 variable name into results line
                    resultsLine.append(line.substring(firstBar + 1, lastBar));
                    continue;
                }
                // processing lines immediately following define-fun lines
                if (!declarationLine) {
                    declarationLine = true;
                    String value = line.substring(0, line.lastIndexOf(')'));

                    if (value.contains("-")) { // negative number
                        // remove brackets surrounding negative numbers
                        value = value.substring(1, value.length() - 1);
                        // remove space between - and the number itself
                        // TODO: clean up string operations.
                        value = String.join("", value.split(" "));
                    }
                    resultsLine.append(" " + value);
                    results.add(resultsLine.toString());
                    resultsLine.setLength(0);
                }
            }
        }
    }

    private String readStdoutByLine(BufferedReader stdout) {
        try {
            return stdout.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Prints arithmetic constraints for debugging
     */
    private void printArithmeticConstraints() {
        logger.fine("=== Arithmetic Constraints Printout ===");
        Map<ArithmeticOperationKind, Integer> arithmeticConstraintCounters = new HashMap<>();
        for (ArithmeticOperationKind kind : ArithmeticOperationKind.values()) {
            arithmeticConstraintCounters.put(kind, 0);
        }
        for (Constraint constraint : constraints) {
            if (constraint instanceof ArithmeticConstraint) {
                ArithmeticConstraint arithmeticConstraint = (ArithmeticConstraint) constraint;
                ArithmeticOperationKind kind = arithmeticConstraint.getOperation();
                arithmeticConstraintCounters.put(kind, arithmeticConstraintCounters.get(kind) + 1);
            }
        }
        for (ArithmeticOperationKind kind : ArithmeticOperationKind.values()) {
            logger.fine(
                    " Made arithmetic "
                            + kind.getSymbol()
                            + " constraint: "
                            + arithmeticConstraintCounters.get(kind));
        }
    }
}
