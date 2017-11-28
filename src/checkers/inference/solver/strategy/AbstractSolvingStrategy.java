package checkers.inference.solver.strategy;

import checkers.inference.solver.backend.SolverFactory;

public abstract class AbstractSolvingStrategy implements SolvingStrategy {

    protected final SolverFactory solverFactory;

    public AbstractSolvingStrategy(SolverFactory solverFactory) {
        this.solverFactory = solverFactory;
    }

}
