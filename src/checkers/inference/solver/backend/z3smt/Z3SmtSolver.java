package checkers.inference.solver.backend.z3smt;

import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ImplicationConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import checkers.inference.model.serialization.ToStringSerializer;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
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
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.javacutil.BugInCF;

public class Z3SmtSolver<SlotEncodingT, SlotSolutionT>
        extends Solver<Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT>> {

    public enum Z3SolverEngineArg implements SolverArg {
        /** option to use optimizing mode or not */
        optimizingMode
    }

    protected final Context ctx;
    protected com.microsoft.z3.Optimize solver;
    protected StringBuffer smtFileContents;

    protected static final String z3Program = "z3";
    protected boolean optimizingMode;
    protected boolean getUnsatCore;

    // used in non-optimizing mode to find unsat constraints
    protected final Map<String, Constraint> serializedConstraints = new HashMap<>();
    protected final List<String> unsatConstraintIDs = new ArrayList<>();

    // file is written at projectRootFolder/constraints.smt
    protected static final String pathToProject =
            new File(new File("").getAbsolutePath()).toString();
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

        // add timeout arg
        if (withTimeout()) {
            z3Args.put("timeout", Integer.toString(timeout()));
        }
        // creating solver
	    ctx = new Context(z3Args);

        z3SmtFormatTranslator.init(ctx);
    }

    protected boolean withTimeout() {
        return false;
    }

    protected int timeout() {
        return 2 * 60 * 1000; // timeout of 2 mins by default
    }

    // Main entry point
    @Override
    public Map<Integer, AnnotationMirror> solve() {
        // serialize based on user choice of running in optimizing or non-optimizing mode
        optimizingMode = solverEnvironment.getBoolArg(Z3SolverEngineArg.optimizingMode);
        getUnsatCore = false;

        if (optimizingMode) {
            System.err.println("Encoding for optimizing mode");
        } else {
            System.err.println("Encoding for non-optimizing mode");
        }

        serializeSMTFileContents();

        solvingStart = System.currentTimeMillis();
        // in Units, if the status is SAT then there must be output in the model
        List<String> results = runZ3Solver();
        solvingEnd = System.currentTimeMillis();

        Statistics.addOrIncrementEntry(
                "smt_serialization_time(millisec)", serializationEnd - serializationStart);
        Statistics.addOrIncrementEntry("smt_solving_time(millisec)", solvingEnd - solvingStart);
        
        System.err.println("=== Arithmetic Constraints Printout ===");
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
            System.err.println(
                    " Made arithmetic "
                            + kind.getSymbol()
                            + " constraint: "
                            + arithmeticConstraintCounters.get(kind));
        }

        if (results == null) {
            System.err.println("\n\n!!! The set of constraints is unsatisfiable! !!!");
            return null;
        }
        
        return formatTranslator.decodeSolution(
                        results, solverEnvironment.processingEnvironment);
    }

    @Override
    public Collection<Constraint> explainUnsatisfiable() {
        optimizingMode = false;
        getUnsatCore = true;

        System.err.println("Now encoding for unsat core dump.");
        serializeSMTFileContents();

        solvingStart = System.currentTimeMillis();
        runZ3Solver();
        solvingEnd = System.currentTimeMillis();

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
        smtFileContents = new StringBuffer();

        // only enable in non-optimizing mode
        if (!optimizingMode && getUnsatCore) {
            smtFileContents.append("(set-option :produce-unsat-cores true)\n");
        }

        serializationStart = System.currentTimeMillis();
        encodeAllSlots();
        encodeAllConstraints();
        if (optimizingMode) {
            encodeAllSoftConstraints();
        }
        serializationEnd = System.currentTimeMillis();

        System.err.println("Encoding constraints done!");

        smtFileContents.append("(check-sat)\n");
        if (!optimizingMode && getUnsatCore) {
            smtFileContents.append("(get-unsat-core)\n");
        } else {
            smtFileContents.append("(get-model)\n");
        }
        
        System.err.println("Writing constraints to file: " + constraintsFile);

        writeConstraintsToSMTFile();
    }

    private void writeConstraintsToSMTFile() {
        String fileContents = smtFileContents.toString();

        if (!getUnsatCore) {
            // write the constraints to the file for external solver use
            FileUtils.writeFile(new File(constraintsFile), fileContents);
        } else {
            // write the constraints to the file for external solver use
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
            if (slot.isVariable()) {
                BoolExpr wfConstraint = formatTranslator.encodeSlotWellformnessConstraint(slot);

                if (!wfConstraint.simplify().isTrue()) {
                    solver.Assert(wfConstraint);
                }
                if (optimizingMode) {
                	encodeSlotPreferenceConstraint(slot);
                }
            }
        }

        // solver.toString() also includes "(check-sat)" as the last line,
        // remove it
        String slotDefinitionsAndConstraints = solver.toString();
        int truncateIndex = slotDefinitionsAndConstraints.lastIndexOf("(check-sat)");
        assert truncateIndex != -1;

        // append slot definitions to overall smt file
        smtFileContents.append(slotDefinitionsAndConstraints.substring(0, truncateIndex));
    }

    @Override
    protected void encodeAllConstraints() {
        int current = 1;

        StringBuffer constraintSmtFileContents = new StringBuffer();

        for (Constraint constraint : constraints) {
            BoolExpr serializedConstraint = constraint.serialize(formatTranslator);

            if (serializedConstraint == null) {
                // TODO: Should error abort if unsupported constraint detected.
                // Currently warning is a workaround for making ontology
                // working, as in some cases existential constraints generated.
                // Should investigate on this, and change this to ErrorAbort
                // when eliminated unsupported constraints.
                System.err.println(
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

            if (!optimizingMode && getUnsatCore) {
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

        String constraintSmt = constraintSmtFileContents.toString();

        smtFileContents.append(constraintSmt);
    }

    protected void encodeAllSoftConstraints() {
    	final Z3SmtSoftConstraintEncoder<SlotEncodingT, SlotSolutionT> encoder = formatTranslator.createSoftConstraintEncoder();
        smtFileContents.append(encoder.encodeAndGetSoftConstraints(constraints));
    }

    protected void encodeSlotPreferenceConstraint(Slot varSlot) {
        // empty string means no optimization group
        solver.AssertSoft(
                formatTranslator.encodeSlotPreferenceConstraint(varSlot), 1, "");
    }

    private List<String> runZ3Solver() {
        // TODO: add z3 stats?
        String[] command;
        if (!getUnsatCore) {
            command = new String[] {z3Program, constraintsFile};
        } else {
            command = new String[] {z3Program, constraintsUnsatCoreFile};
        }

        // stores results from z3 program output
        final List<String> results = new ArrayList<>();

        // Run command
        // TODO: check that stdErr has no errors
        int exitStatus =
                ExternalSolverUtils.runExternalSolver(
                        command,
                        stdOut -> parseStdOut(stdOut, results),
                        stdErr -> ExternalSolverUtils.printStdStream(System.err, stdErr));
        // if exit status from z3 is not 0, then it is unsat
        return exitStatus == 0 ? results : null;
    }

    // parses the STD output from the z3 process and handles SAT and UNSAT outputs
    private void parseStdOut(BufferedReader stdOut, List<String> results) {
        String line = "";

        boolean declarationLine = true;
        // each result line is "varName value"
        String resultsLine = "";

        boolean unsat = false;

        try {
            while ((line = stdOut.readLine()) != null) {
                line = line.trim();

                if (getUnsatCore) {
                    // UNSAT Cases ====================
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
                            unsatConstraintIDs.add(constraintID);
                        }
                        continue;
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
                        resultsLine += line.substring(firstBar + 1, lastBar);
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
                            value = String.join("", value.split(" "));
                        }

                        resultsLine += " " + value;
                        results.add(resultsLine);
                        resultsLine = "";
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
