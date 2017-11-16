package checkers.inference.solver.strategy;

import checkers.inference.InferenceMain;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.util.ConstraintVerifier;

public abstract class AbstractSolveStrategy implements SolveStrategy {

    protected final SolverFactory solverFactory;
    //TODO: where is the best place for referring this verifier?
    protected final ConstraintVerifier verifier;

    public AbstractSolveStrategy(SolverFactory solverFactory) {
        this.solverFactory = solverFactory;
        this.verifier = InferenceMain.getInstance().getConstraintManager().getConstraintVerifier();
    }

}
