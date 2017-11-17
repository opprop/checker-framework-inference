package checkers.inference.solver.strategy;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.solver.backend.SolverFactory;

public class StrategyReflectiveFactory {

    private static final String STRATEGY_PACKAGE_NAME = StrategyReflectiveFactory.class.getPackage().getName();

    public static SolvingStrategy createSolvingStrategy(String strategy, SolverFactory solverFactory) {
        // Set default strategy to plain solve strategy.
        strategy = strategy == null ? "Plain" : strategy;

        final String strategyClassName = strategy + "SolvingStrategy";

        try {
            Class<?> solverStrategyClass = Class.forName(STRATEGY_PACKAGE_NAME + "." + strategyClassName);
            return (SolvingStrategy) solverStrategyClass.getConstructor(SolverFactory.class).newInstance(solverFactory);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when creating " + strategy + " solve strategy!", e);
            return null;
        }
    }
}
