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
import checkers.inference.solver.strategy.PlainSolvingStrategy;
import checkers.inference.solver.strategy.SolvingStrategy;
import checkers.inference.solver.strategy.StrategyReflectiveFactory;
import checkers.inference.solver.util.NameUtils;
import checkers.inference.solver.util.PrintUtils;
import checkers.inference.solver.util.SolverArg;
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
    protected String strategyName;


    protected enum SolverEngineArg implements SolverArg {
        solvingStrategy,
        collectStatistic;
    }

    protected SolverFactory createSolverFactory() {
        return new DefaultSolverFactory();
    }

    protected SolvingStrategy createSolvingStrategy() {
        SolverFactory solverFactory = createSolverFactory();
        return StrategyReflectiveFactory.createSolvingStrategy(strategyName, solverFactory);
    }

    @Override
    public final InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        SolverOptions solverOptions = new SolverOptions(configuration);

        InferenceSolution solution = null;

        configureSolverArgs(solverOptions);

        //TODO: Add solve timing statistic.
        SolvingStrategy solvingStrategy = createSolvingStrategy();
        solution = solvingStrategy.solve(solverOptions, slots, constraints, qualHierarchy, processingEnvironment);

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
     * This method configures following arguments: solving strategy, and collectStatistic.
     * 
     * @param configuration
     */
    private void configureSolverArgs(SolverOptions solverOptions) {
        String strategyName = solverOptions.getArg(SolverEngineArg.solvingStrategy);
        this.strategyName = strategyName == null ?
                NameUtils.removeSuffix(PlainSolvingStrategy.class.getSimpleName(), SolvingStrategy.class.getName())
                : strategyName;

        this.collectStatistic = solverOptions.getBoolArg(SolverEngineArg.collectStatistic);
        // Sanitize the configuration if it needs.
        sanitizeConfiguration();
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

        final String CONSTANT_SLOT_NAME = ConstantSlot.class.getSimpleName();
        final String VARIABLE_SLOT_NAME = VariableSlot.class.getSimpleName();
        for (Slot slot : slots) {
            if (slot instanceof ConstantSlot) {
                if (!modelMap.containsKey(CONSTANT_SLOT_NAME)) {
                    modelMap.put(CONSTANT_SLOT_NAME, 1);
                } else {
                    modelMap.put(CONSTANT_SLOT_NAME, modelMap.get(CONSTANT_SLOT_NAME) + 1);
                }

            } else if (slot instanceof VariableSlot) {
                if (!modelMap.containsKey(VARIABLE_SLOT_NAME)) {
                    modelMap.put(VARIABLE_SLOT_NAME, 1);
                } else {
                    modelMap.put(VARIABLE_SLOT_NAME, modelMap.get(VARIABLE_SLOT_NAME) + 1);
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
