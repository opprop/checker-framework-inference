package dataflow.solvers.backend.z3;

import checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator;
import checkers.inference.solver.backend.z3.Z3SolverFactory;
import checkers.inference.solver.frontend.Lattice;


public class DataflowZ3SolverFactory extends Z3SolverFactory {

    @Override
    protected Z3BitVectorFormatTranslator createFormatTranslator(Lattice lattice) {
        return new DataflowZ3FormatTranslator(lattice);
    }
}
