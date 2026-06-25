package checkers.inference.solver.backend.javasmt;

import org.checkerframework.javacutil.BugInCF;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverArg;
import checkers.inference.solver.util.SolverEnvironment;

/** Solver backend that uses JavaSMT with bit-vector encodings. */
public class JavaSmtSolver extends Solver<JavaSmtFormatTranslator> {

    public enum JavaSmtSolverArg implements SolverArg {
        /** JavaSMT concrete solver. Defaults to Z3. */
        javasmtSolver
    }

    private static final Solvers DEFAULT_JAVASMT_SOLVER = Solvers.Z3;

    public JavaSmtSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            JavaSmtFormatTranslator formatTranslator,
            Lattice lattice) {
        super(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    @Override
    public Map<Integer, AnnotationMirror> solve() {
        Solvers javaSmtSolver = getJavaSmtSolver();
        try (SolverContext context = JavaSmtSolverContexts.create(javaSmtSolver);
                ProverEnvironment prover =
                        context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
            formatTranslator.init(context);
            collectVarSlotsFromSlots();
            encodeAllConstraints(prover);

            if (prover.isUnsat()) {
                return null;
            }

            try (Model model = prover.getModel()) {
                return decodeSolution(model);
            }
        } catch (Exception e) {
            throw new BugInCF("JavaSMT solver failed", e);
        }
    }

    @Override
    public Collection<Constraint> explainUnsatisfiable() {
        return new HashSet<>();
    }

    @Override
    protected void encodeAllConstraints() {
        throw new BugInCF("Use encodeAllConstraints(ProverEnvironment) for JavaSMT");
    }

    private void encodeAllConstraints(ProverEnvironment prover) throws InterruptedException {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);

            if (constraint instanceof PreferenceConstraint) {
                InferenceMain.getInstance()
                        .logger
                        .warning(
                                "JavaSMT backend does not yet support soft preference constraints;"
                                        + " skipping "
                                        + constraint);
                continue;
            }

            BooleanFormula serializedConstraint = constraint.serialize(formatTranslator);
            if (serializedConstraint == null) {
                throw new BugInCF("Unsupported JavaSMT constraint detected: " + constraint);
            }

            if (formatTranslator.getBooleanFormulaManager().isTrue(serializedConstraint)) {
                continue;
            }

            prover.addConstraint(serializedConstraint);
        }
    }

    private void collectVarSlotsFromSlots() {
        for (Slot slot : slots) {
            if (!(slot instanceof ConstantSlot)) {
                varSlotIds.add(slot.getId());
                slot.serialize(formatTranslator);
            }
        }
    }

    private Map<Integer, AnnotationMirror> decodeSolution(Model model) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();

        for (Integer slotId : varSlotIds) {
            BitvectorFormula serializedSlot = formatTranslator.getSerializedSlot(slotId);
            if (serializedSlot == null) {
                continue;
            }
            BigInteger solution = model.evaluate(serializedSlot);
            result.put(
                    slotId,
                    formatTranslator.decodeSolution(
                            solution, solverEnvironment.processingEnvironment));
        }

        return result;
    }

    private Solvers getJavaSmtSolver() {
        String solverName = solverEnvironment.getArg(JavaSmtSolverArg.javasmtSolver);
        if (solverName == null) {
            return DEFAULT_JAVASMT_SOLVER;
        }
        try {
            return Solvers.valueOf(solverName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BugInCF("Unknown JavaSMT solver: " + solverName, e);
        }
    }
}
