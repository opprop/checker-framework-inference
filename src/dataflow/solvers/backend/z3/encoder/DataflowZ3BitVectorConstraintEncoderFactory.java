package dataflow.solvers.backend.z3.encoder;

import checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator;
import checkers.inference.solver.backend.z3.encoder.Z3BitVectorConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;

import com.microsoft.z3.Context;

public class DataflowZ3BitVectorConstraintEncoderFactory extends
    Z3BitVectorConstraintEncoderFactory {

    public DataflowZ3BitVectorConstraintEncoderFactory(
        Lattice lattice,
        Context context,
        Z3BitVectorFormatTranslator formatTranslator) {
        super(lattice, context, formatTranslator);
    }
}
