package checkers.inference.solver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceResult;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceResult;
import checkers.inference.InferenceSolver;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.serialization.CnfVecIntSerializer;

/**
 * This solver is used to convert any constraint set using a type system with only 2 types
 * (Top/Bottom), into a SAT problem. This SAT problem is then solved by SAT4J and the output is
 * converted back into an InferenceResult.
 */
public class MaxSat2TypeSolver implements InferenceSolver {

    // private QualifierHierarchy qualHierarchy;
    private Collection<Constraint> constraints;
    // private Collection<Slot> slots;

    // private AnnotationMirror defaultValue;
    private AnnotationMirror top;
    private AnnotationMirror bottom;
    private CnfVecIntSerializer serializer;
    private SlotManager slotManager;

    @Override
    public InferenceResult solve(
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        // this.slots = slots;
        this.constraints = constraints;
        // this.qualHierarchy = qualHierarchy;

        this.top = qualHierarchy.getTopAnnotations().iterator().next();
        this.bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.serializer =
                new CnfVecIntSerializer(slotManager) {
                    @Override
                    protected boolean isTop(ConstantSlot constantSlot) {
                        return AnnotationUtils.areSame(constantSlot.getValue(), top);
                    }
                };
        // TODO: This needs to be parameterized based on the type system
        // this.defaultValue = top;

        return solve();
    }

    public InferenceResult solve() {

        final List<VecInt> softClauses = new LinkedList<>();
        final List<VecInt> hardClauses = new LinkedList<>();
        serializer.convertAll(constraints, hardClauses, softClauses);

        // nextId describes the LARGEST id that might be found in a variable
        // if an exception occurs while creating a variable the id might be incremented
        // but the slot might not actually be recorded.  Therefore, nextId is NOT
        // the number of slots but the maximum you might encounter.
        // TODO: this is a workaround as currently when serialize existential constraint we lost the
        // real existential
        // TODO: variable id and create "fake" id stored in existentialToPotentialVar map.
        // TODO: thus here the value of totalVars is the real slots number stored in slotManager,
        // and plus the
        // TODO: "fake" slots number stored in existentialToPotentialVar
        final int totalVars =
                slotManager.getNumberOfSlots() + serializer.getExistentialToPotentialVar().size();
        final int totalClauses = hardClauses.size() + softClauses.size();

        // When .newBoth is called, SAT4J will run two solvers and return the result of the first to
        // halt
        final WeightedMaxSatDecorator solver =
                new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);

        // arbitrary timeout selected for no particular reason
        solver.setTimeoutMs(1000000);

        VecInt lastClause = null;
        try {
            for (VecInt clause : hardClauses) {
                lastClause = clause;
                solver.addHardClause(clause);
            }
            for (VecInt clause : softClauses) {

                lastClause = clause;
                solver.addSoftClause(clause);
            }

        } catch (ContradictionException ce) {
            // This happens when adding a clause causes trivial contradiction, such as adding -1 to
            // {1}
            System.out.println(
                    "Not solvable! Contradiction exception "
                            + "when adding clause: "
                            + lastClause
                            + ".");

            // pass empty set as the unsat explanation
            // TODO: explain UNSAT possibly by reusing
            // MaxSatSolver.MaxSATUnsatisfiableConstraintExplainer
            return new DefaultInferenceResult(new HashSet<>());
        }

        boolean isSatisfiable;
        try {
            // isSatisfiable() launches the solvers and waits until one of them finishes
            isSatisfiable = solver.isSatisfiable();

        } catch (TimeoutException te) {
            throw new RuntimeException("MAX-SAT solving timeout! ");
        }

        if (!isSatisfiable) {
            System.out.println("Not solvable!");
            // pass empty set as the unsat explanation
            // TODO: explain UNSAT possibly by reusing
            // MaxSatSolver.MaxSATUnsatisfiableConstraintExplainer
            return new DefaultInferenceResult(new HashSet<>());
        }

        int[] solution = solver.model();
        // The following code decodes VecInt solution to the slot-annotation mappings
        final Map<Integer, AnnotationMirror> decodedSolution = new HashMap<>();
        final Map<Integer, Integer> existentialToPotentialIds =
                serializer.getExistentialToPotentialVar();

        for (Integer var : solution) {
            Integer potential = existentialToPotentialIds.get(Math.abs(var));
            if (potential != null) {
                // Assume the 'solution' output by the solver is already sorted in the ascending
                // order
                // of their absolute values. So the existential variables come after the potential
                // variables,
                // which means the potential slot corresponding to the current existential variable
                // is
                // already inserted into 'solutions'
                assert decodedSolution.containsKey(potential);
                if (var < 0) {
                    // The existential variable is false, so the potential variable should not be
                    // inserted.
                    // Remove it from the solution.
                    decodedSolution.remove(potential);
                }
            } else {
                boolean isTop = var < 0;
                if (isTop) {
                    var = -var;
                }
                decodedSolution.put(var, isTop ? top : bottom);
            }
        }

        return new DefaultInferenceResult(decodedSolution);
    }
}
