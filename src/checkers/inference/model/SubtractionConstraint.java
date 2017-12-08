package checkers.inference.model;

/**
 * Represents a constraint between two operands of a subtraction and the results of the subtraction.
 */
public class SubtractionConstraint extends ArithmeticConstraint {

    protected SubtractionConstraint(Slot lhs, Slot rhs, Slot result, AnnotationLocation location) {
        super(lhs, rhs, result, location);
    }

    @Override
    public Constraint make(Slot first, Slot second, Slot third, AnnotationLocation location) {
        return new SubtractionConstraint(first, second, third, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
