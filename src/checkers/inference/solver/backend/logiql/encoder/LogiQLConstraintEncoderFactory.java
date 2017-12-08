package checkers.inference.solver.backend.logiql.encoder;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.AdditionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.DivisionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ModulusConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.MultiplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.SubtractionConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;

/**
 * LogiQL implementation of {@link checkers.inference.solver.backend.encoder.ConstraintEncoderFactory}.
 *
 * @see checkers.inference.solver.backend.encoder.ConstraintEncoderFactory
 */
public class LogiQLConstraintEncoderFactory extends AbstractConstraintEncoderFactory<String> {

    public LogiQLConstraintEncoderFactory(Lattice lattice, ConstraintVerifier verifier) {
        super(lattice, verifier);
    }

    @Override
    public SubtypeConstraintEncoder<String> createSubtypeConstraintEncoder() {
        return new LogiQLSubtypeConstraintEncoder(lattice, verifier);
    }

    @Override
    public EqualityConstraintEncoder<String> createEqualityConstraintEncoder() {
        return new LogiQLEqualityConstraintEncoder(lattice, verifier);
    }

    @Override
    public InequalityConstraintEncoder<String> createInequalityConstraintEncoder() {
        return new LogiQLInequalityConstraintEncoder(lattice, verifier);
    }

    @Override
    public ComparableConstraintEncoder<String> createComparableConstraintEncoder() {
        return new LogiQLComparableConstraintEncoder(lattice, verifier);
    }

    @Override
    public PreferenceConstraintEncoder<String> createPreferenceConstraintEncoder() {
        return null;
    }

    @Override
    public CombineConstraintEncoder<String> createCombineConstraintEncoder() {
        return null;
    }

    @Override
    public ExistentialConstraintEncoder<String> createExistentialConstraintEncoder() {
        return null;
    }

    @Override
    public AdditionConstraintEncoder<String> createAdditionConstraintEncoder() {
        return null;
    }

    @Override
    public SubtractionConstraintEncoder<String> createSubtractionConstraintEncoder() {
        return null;
    }

    @Override
    public MultiplicationConstraintEncoder<String> createMultiplicationConstraintEncoder() {
        return null;
    }

    @Override
    public DivisionConstraintEncoder<String> createDivisionConstraintEncoder() {
        return null;
    }

    @Override
    public ModulusConstraintEncoder<String> createModulusConstraintEncoder() {
        return null;
    }
}
