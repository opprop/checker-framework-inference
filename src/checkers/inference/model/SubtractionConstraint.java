package checkers.inference.model;

/**
 * Represents a constraint between two operands of a subtraction and the results of the subtraction.
 */
public class SubtractionConstraint extends ArithmeticConstraint {

    protected SubtractionConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(leftOperand, rightOperand, result, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
