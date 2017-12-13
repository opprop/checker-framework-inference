package checkers.inference.model;

/**
 * Represents a constraint between two operands of a modulus and the results of the modulus.
 */
public class ModulusConstraint extends ArithmeticConstraint {

    protected ModulusConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(leftOperand, rightOperand, result, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
