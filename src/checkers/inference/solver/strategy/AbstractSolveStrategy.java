package checkers.inference.solver.strategy;

import checkers.inference.solver.backend.SolverFactory;

public abstract class AbstractSolveStrategy implements SolveStrategy {

    protected final SolverFactory solverFactory;

    public AbstractSolveStrategy(SolverFactory solverFactory) {
        this.solverFactory = solverFactory;
    }

}
