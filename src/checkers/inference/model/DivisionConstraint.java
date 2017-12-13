package checkers.inference.model;

/**
 * Represents a constraint between two operands of a division and the results of the division.
 */
public class DivisionConstraint extends ArithmeticConstraint {

    protected DivisionConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(leftOperand, rightOperand, result, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
