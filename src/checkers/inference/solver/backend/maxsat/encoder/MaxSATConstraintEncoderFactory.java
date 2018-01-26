package checkers.inference.solver.backend.maxsat.encoder;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import org.sat4j.core.VecInt;
import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ViewpointAdaptationConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;

/**
 * MaxSAT implementation of {@link checkers.inference.solver.backend.encoder.ConstraintEncoderFactory}.
 *
 * @see checkers.inference.solver.backend.encoder.ConstraintEncoderFactory
 */
public class MaxSATConstraintEncoderFactory extends AbstractConstraintEncoderFactory<VecInt[]> {

    private final Map<AnnotationMirror, Integer> typeToInt;

    public MaxSATConstraintEncoderFactory(Lattice lattice, ConstraintVerifier verifier, Map<AnnotationMirror, Integer> typeToInt) {
        super(lattice, verifier);
        this.typeToInt = typeToInt;
    }

    @Override
    public MaxSATSubtypeConstraintEncoder createSubtypeConstraintEncoder() {
        return new MaxSATSubtypeConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public MaxSATEqualityConstraintEncoder createEqualityConstraintEncoder() {
        return new MaxSATEqualityConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public MaxSATInequalityConstraintEncoder createInequalityConstraintEncoder() {
        return new MaxSATInequalityConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public MaxSATComparableConstraintEncoder createComparableConstraintEncoder() {
        return new MaxSATComparableConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public MaxSATPreferenceConstraintEncoder createPreferenceConstraintEncoder() {
        return new MaxSATPreferenceConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public ViewpointAdaptationConstraintEncoder<VecInt[]> createViewpointAdaptationConstraintEncoder() {
        return null;
    }

    @Override
    public ExistentialConstraintEncoder<VecInt[]> createExistentialConstraintEncoder() {
        return null;
    }

    @Override
    public ArithmeticConstraintEncoder<VecInt[]> createArithmeticConstraintEncoder() {
        return null;
    }
}
