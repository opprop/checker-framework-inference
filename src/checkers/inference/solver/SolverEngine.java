package checkers.inference.solver;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.InferenceSolution;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.DefaultSolverFactory;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.strategy.SolveStrategy;
import checkers.inference.solver.strategy.StrategyReflectiveFactory;
import checkers.inference.solver.util.Constants.SlotType;
import checkers.inference.solver.util.PrintUtils;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.solver.util.StatisticRecorder;
import checkers.inference.solver.util.StatisticRecorder.StatisticKey;

/**
 * GeneralSolver is the entry point of general solver framework, and it is also
 * the front end of whole solver system. GeneralSolver configures command line
 * arguments, creates corresponding back end(s) and serializer, invokes the back
 * end(s) and returns the solution.
 * 
 * @author jianchu
 *
 */

public class SolverEngine implements InferenceSolver {
    protected boolean collectStatistic;
    protected final SolverFactory solverFactory;
    protected String strategyName;

    public SolverEngine() {
        solverFactory = createSolverFactory();
    }

    protected enum SolverEngineArg {
        solveStrategy,
        collectStatistic;
    }

    protected SolverFactory createSolverFactory() {
        return new DefaultSolverFactory();
    }

    protected SolveStrategy createSolveStrategy(String strategyName) {
        return StrategyReflectiveFactory.createSolveStrategy(strategyName);
    }

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        SolverOptions solverOptions = new SolverOptions(configuration);

        InferenceSolution solution = null;

        configureSolverArgs(solverOptions);

        //TODO: Add solve timing statistic.
        SolveStrategy solveStrategy = createSolveStrategy(strategyName);
        solution = solveStrategy.solve(solverOptions, slots, constraints, qualHierarchy, processingEnvironment);

        if (solution == null) {
            // Solution should never be null.
            ErrorReporter.errorAbort("Null solution detected!");
        }

        if (collectStatistic) {
            Map<String, Integer> modelRecord = recordSlotConstraintSize(slots, constraints);
            PrintUtils.printStatistic(StatisticRecorder.getStatistic(), modelRecord);
            PrintUtils.writeStatistic(StatisticRecorder.getStatistic(), modelRecord);
        }

        return solution;
    }

    /**
     * This method configures following arguments: backEndType, useGraph,
     * solveInParallel, and collectStatistic
     * 
     * @param configuration
     */
    private void configureSolverArgs(SolverOptions solverOptions) {
       final String strategyName = solverOptions.getArg(SolverEngineArg.solveStrategy.name());
       this.strategyName = strategyName == null ? "plain" : strategyName;
       this.collectStatistic = solverOptions.getBoolArg(SolverEngineArg.collectStatistic.name());

        // Sanitize the configuration if it needs.
        sanitizeConfiguration();
        System.out.println("Configuration: \n solveStrategy: " + strategyName);
    }

    /**
     * Sanitize and apply check of the configuration of solver based on a
     * specific type system. Sub-class solver of a specific type system may
     * override this method to sanitize the configuration of solver in the
     * context of that type system.
     */
    protected void sanitizeConfiguration() {
        //Intentionally empty.
    }

    //TODO: Move this method to the class responsible for statistic.
    /**
     * Method that counts the size of each kind of constraint and slot.
     * 
     * @param slots
     * @param constraints
     * @return A map between name of constraint/slot and their counts.
     */
    private Map<String, Integer> recordSlotConstraintSize(final Collection<Slot> slots,
            final Collection<Constraint> constraints) {

        // Record constraint size
        StatisticRecorder.record(StatisticKey.CONSTRAINT_SIZE, (long) constraints.size());
        // Record slot size
        StatisticRecorder.record(StatisticKey.SLOTS_SIZE, (long) slots.size());
        Map<String, Integer> modelMap = new LinkedHashMap<>();

        for (Slot slot : slots) {
            if (slot instanceof ConstantSlot) {
                if (!modelMap.containsKey(SlotType.ConstantSlot.name())) {
                    modelMap.put(SlotType.ConstantSlot.name(), 1);
                } else {
                    modelMap.put(SlotType.ConstantSlot.name(), modelMap.get(SlotType.ConstantSlot.name()) + 1);
                }

            } else if (slot instanceof VariableSlot) {
                if (!modelMap.containsKey(SlotType.VariableSlot.name())) {
                    modelMap.put(SlotType.VariableSlot.name(), 1);
                } else {
                    modelMap.put(SlotType.VariableSlot.name(), modelMap.get(SlotType.VariableSlot.name()) + 1);
                }
            }
        }

        for (Constraint constraint : constraints) {
            String simpleName = constraint.getClass().getSimpleName();
            if (!modelMap.containsKey(simpleName)) {
                modelMap.put(simpleName, 1);
            } else {
                modelMap.put(simpleName, modelMap.get(simpleName) + 1);
            }
        }
        return modelMap;
    }

}
