package checkers.inference.solver.strategy;

import org.checkerframework.javacutil.ErrorReporter;

public class StrategyReflectiveFactory {

    private static final String STRATEGY_PACKAGE_NAME = StrategyReflectiveFactory.class.getPackage().getName();

    public static SolveStrategy createSolveStrategy(String strategy) {
        final String strategyClassName = strategy.substring(0, 1).toUpperCase() + strategy.substring(1) + "SolveStrategy"; 
        try {
            Class<?> solverStrategyClass = Class.forName(STRATEGY_PACKAGE_NAME + "." + strategyClassName);
            return (SolveStrategy) solverStrategyClass.getConstructor().newInstance();
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when creating " + strategy + " solve strategy!", e);
            return null;
        }
    }
}
