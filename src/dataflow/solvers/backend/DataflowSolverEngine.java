package dataflow.solvers.backend;

import checkers.inference.solver.SolverEngine;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.backend.z3.Z3Solver;
import checkers.inference.solver.util.NameUtils;

import dataflow.solvers.backend.z3.DataflowZ3SolverFactory;

public class DataflowSolverEngine extends SolverEngine {

    @Override
    protected SolverFactory createSolverFactory() {
        if (NameUtils.getSolverName(Z3Solver.class).equals(solverName)) {
            return new DataflowZ3SolverFactory();
        } else {
            return super.createSolverFactory();
        }
    }
}
