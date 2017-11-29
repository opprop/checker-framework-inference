package checkers.inference.solver.backend.maxsat;

import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;
import checkers.inference.solver.backend.FailureExplainer;
import checkers.inference.solver.frontend.Lattice;
import org.checkerframework.javacutil.ErrorReporter;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.DeletionStrategy;
import org.sat4j.tools.xplain.Xplain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MaxSAT implementation of FailureExplainer.
 */
public class MaxSATFailureExplainer implements FailureExplainer {

    private final List<VecInt> hardClauses;
    private final List<Constraint> hardConstraints;
    private final SlotManager slotManager;
    private final Lattice lattice;

    public MaxSATFailureExplainer(List<VecInt> hardClauses, List<Constraint> hardConstraints,
                                  SlotManager slotManager, Lattice lattice) {
        this.hardClauses = hardClauses;
        this.hardConstraints = hardConstraints;
        this.slotManager = slotManager;
        this.lattice = lattice;
    }

    @Override
    public Collection<Constraint> minimumUnsatisfiableConstraints() {
        Set<Constraint> musSet = new HashSet<>();
        // Explainer solver that is used
        Xplain<IPBSolver> explanationSolver = new Xplain<>(SolverFactory.newDefault());
        configureExplanationSolver(hardClauses, slotManager, lattice, explanationSolver);
        try {
            for (VecInt clause : hardClauses) {
                explanationSolver.addClause(clause);
            }
            assert !explanationSolver.isSatisfiable();
            int[] indicies = explanationSolver.minimalExplanation();
            for (int clauseIndex : indicies) {
                if (clauseIndex > hardConstraints.size()) continue;
                // Solver gives 1-based index. Decrement by 1 here to get stored constraint
                Constraint constraint = hardConstraints.get(clauseIndex - 1);
                musSet.add(constraint);
            }
        } catch (TimeoutException e) {
            ErrorReporter.errorAbort("Explanation solver encountered time out", e);
        } catch (ContradictionException e) {
            ErrorReporter.errorAbort("Explanation solver shouldn never encounter ContradictionException" +
                    "as it tries to find contradictions among constraints, but it did!", e);
        }
        return musSet;
    }

    private void configureExplanationSolver(final List<VecInt> hardClauses, final SlotManager slotManager, final Lattice lattice, final Xplain<IPBSolver> explainer) {
        int numberOfNewVars = slotManager.getNumberOfSlots() * lattice.numTypes;
        int numberOfClauses = hardClauses.size();
        explainer.setMinimizationStrategy(new DeletionStrategy());
        explainer.newVar(numberOfNewVars);
        explainer.setExpectedNumberOfClauses(numberOfClauses);
    }
}
