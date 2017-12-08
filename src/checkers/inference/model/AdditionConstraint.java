package checkers.inference.model;

/**
 * Represents a constraint between two operands of an addition and the results of the addition.
 */
public class AdditionConstraint extends ArithmeticConstraint {

    protected AdditionConstraint(Slot lhs, Slot rhs, Slot result, AnnotationLocation location) {
        super(lhs, rhs, result, location);
    }

    @Override
    public Constraint make(Slot first, Slot second, Slot third, AnnotationLocation location) {
        return new AdditionConstraint(first, second, third, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
