package refval.solvers.general;

import checkers.inference.InferenceMain;
import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.strategy.GraphSolvingStrategy;
import checkers.inference.solver.strategy.SolvingStrategy;
import checkers.inference.solver.util.NameUtils;

/**
 * RefValSolverEngine is the solver for RefVal type system. It encode
 * RefVal type hierarchy as two qualifiers type system.
 * 
 * @author jianchu
 *
 */
public class RefValSolverEngine extends SolverEngine {

    @Override
    protected SolvingStrategy createSolvingStrategy(SolverFactory solverFactory) {
        return new RefValGraphSolvingStrategy(solverFactory);
    }

    @Override
    protected void sanitizeSolverEngineArgs() {
        if (!NameUtils.getStrategyName(GraphSolvingStrategy.class).equals(strategyName)) {
            InferenceMain.getInstance().logger.warning("RefVal type system must use graph solve strategy."
                    + "Change strategy from " + strategyName + " to graph.");
            strategyName = NameUtils.getStrategyName(GraphSolvingStrategy.class);
        }
    }
}
