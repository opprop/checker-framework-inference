package checkers.inference.model;

/**
 * Represents a constraint between two operands of an addition and the results of the addition.
 */
public class AdditionConstraint extends ArithmeticConstraint {

    protected AdditionConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(leftOperand, rightOperand, result, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
