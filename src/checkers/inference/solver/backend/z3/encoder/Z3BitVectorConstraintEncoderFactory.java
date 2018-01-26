package checkers.inference.solver.backend.z3.encoder;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ViewpointAdaptationConstraintEncoder;
import checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;

/**
 * Z3 implementation of {@link checkers.inference.solver.backend.encoder.ConstraintEncoderFactory}.
 *
 * @see checkers.inference.solver.backend.encoder.ConstraintEncoderFactory
 */
public class Z3BitVectorConstraintEncoderFactory extends AbstractConstraintEncoderFactory<BoolExpr>{

    private final Context context;
    private final Z3BitVectorFormatTranslator z3BitVectorFormatTranslator;

    public Z3BitVectorConstraintEncoderFactory(Lattice lattice, ConstraintVerifier verifier,
                                               Context context, Z3BitVectorFormatTranslator z3BitVectorFormatTranslator) {
        super(lattice, verifier);
        this.context = context;
        this.z3BitVectorFormatTranslator = z3BitVectorFormatTranslator;
    }

    @Override
    public Z3BitVectorSubtypeConstraintEncoder createSubtypeConstraintEncoder() {
        return new Z3BitVectorSubtypeConstraintEncoder(lattice, verifier, context, z3BitVectorFormatTranslator);
    }

    @Override
    public EqualityConstraintEncoder<BoolExpr> createEqualityConstraintEncoder() {
        return new Z3BitVectorEqualityConstraintEncoder(lattice, verifier, context, z3BitVectorFormatTranslator);
    }

    @Override
    public InequalityConstraintEncoder<BoolExpr> createInequalityConstraintEncoder() {
        // TODO InequalityEncoder can be supported.
        return null;
    }

    @Override
    public ComparableConstraintEncoder<BoolExpr> createComparableConstraintEncoder() {
        return null;
    }

    @Override
    public PreferenceConstraintEncoder<BoolExpr> createPreferenceConstraintEncoder() {
        return new Z3BitVectorPreferenceConstraintEncoder(lattice, verifier, context, z3BitVectorFormatTranslator);
    }

    @Override
    public ViewpointAdaptationConstraintEncoder<BoolExpr> createViewpointAdaptationConstraintEncoder() {
        return null;
    }

    @Override
    public ExistentialConstraintEncoder<BoolExpr> createExistentialConstraintEncoder() {
        return null;
    }

    @Override
    public ArithmeticConstraintEncoder<BoolExpr> createArithmeticConstraintEncoder() {
        return null;
    }
}
