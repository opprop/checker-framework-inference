package checkers.inference.model;

/**
 * Represents a constraint between two operands of a multiplication and the results of the
 * multiplication.
 */
public class MultiplicationConstraint extends ArithmeticConstraint {

    protected MultiplicationConstraint(Slot lhs, Slot rhs, Slot result,
            AnnotationLocation location) {
        super(lhs, rhs, result, location);
    }

    @Override
    public Constraint make(Slot first, Slot second, Slot third, AnnotationLocation location) {
        return new MultiplicationConstraint(first, second, third, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
