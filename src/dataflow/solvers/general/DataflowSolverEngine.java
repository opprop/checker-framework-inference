package dataflow.solvers.general;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.strategy.SolveStrategy;

/**
 * DataflowGeneralSolver is the solver for dataflow type system. It encode
 * dataflow type hierarchy as two qualifiers type system.
 * 
 * @author jianchu
 *
 */
public class DataflowSolverEngine extends SolverEngine {

    @Override
    protected SolveStrategy createSolveStrategy(String strategyName) {
        switch(strategyName) {
            case "graph": {
                return new DataflowGraphSolveStrategy(solverFactory);
            }
            default: {
                return super.createSolveStrategy(strategyName);
            }
        }
    }

}
