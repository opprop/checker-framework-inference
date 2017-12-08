package checkers.inference.solver.backend.z3.encoder;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.AdditionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.DivisionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ModulusConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.MultiplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.SubtractionConstraintEncoder;
import checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

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
    public CombineConstraintEncoder<BoolExpr> createCombineConstraintEncoder() {
        return null;
    }

    @Override
    public ExistentialConstraintEncoder<BoolExpr> createExistentialConstraintEncoder() {
        return null;
    }

    @Override
    public AdditionConstraintEncoder<BoolExpr> createAdditionConstraintEncoder() {
        return null;
    }

    @Override
    public SubtractionConstraintEncoder<BoolExpr> createSubtractionConstraintEncoder() {
        return null;
    }

    @Override
    public MultiplicationConstraintEncoder<BoolExpr> createMultiplicationConstraintEncoder() {
        return null;
    }

    @Override
    public DivisionConstraintEncoder<BoolExpr> createDivisionConstraintEncoder() {
        return null;
    }

    @Override
    public ModulusConstraintEncoder<BoolExpr> createModulusConstraintEncoder() {
        return null;
    }
}
