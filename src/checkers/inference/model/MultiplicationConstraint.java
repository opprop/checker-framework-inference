package checkers.inference.model;

/**
 * Represents a constraint between two operands of a multiplication and the results of the
 * multiplication.
 */
public class MultiplicationConstraint extends ArithmeticConstraint {

    protected MultiplicationConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(leftOperand, rightOperand, result, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
