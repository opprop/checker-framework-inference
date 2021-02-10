package dataflow.solvers.backend.z3;

import checkers.inference.solver.backend.z3.Z3BitVectorCodec;
import checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

public class DataflowZ3FormatTranslator extends Z3BitVectorFormatTranslator {

    public DataflowZ3FormatTranslator(Lattice lattice) {
        super(lattice);
    }

    @Override
    protected Z3BitVectorCodec createZ3BitVectorCodec() {
        return new DataflowZ3BitVectorCodec();
    }
}
