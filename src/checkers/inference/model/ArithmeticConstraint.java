package checkers.inference.model;

import java.util.Arrays;

/**
 * Represents a constraint between the result of an arithmetic operation and its two operands.
 * Subclasses of this constraint class denote each specific kind of arithmetic operation, such as
 * add, sub, mul, div, and mod.
 *
 * <p>
 * This abstract class defines the fields and accessors shared by all arithmetic operations.
 */
public class ArithmeticConstraint extends Constraint implements TernaryConstraint {

    public enum ArithmeticOperationKind {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULUS
    }

    private final ArithmeticOperationKind operation;
    private final Slot leftOperand;
    private final Slot rightOperand;
    private final Slot result;

    protected ArithmeticConstraint(ArithmeticOperationKind operation, Slot leftOperand,
            Slot rightOperand, Slot result, AnnotationLocation location) {
        super(Arrays.asList(leftOperand, rightOperand, result), location);
        this.operation = operation;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.result = result;
    }

    public ArithmeticOperationKind getOperation() {
        return operation;
    }

    @Override
    public Slot getLeftOperand() {
        return leftOperand;
    }

    @Override
    public Slot getRightOperand() {
        return rightOperand;
    }

    @Override
    public Slot getResult() {
        return result;
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public int hashCode() {
        final int p = 31;
        int r = 1;
        r = p * r + ((operation == null) ? 0 : operation.hashCode());
        r = p * r + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        r = p * r + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        r = p * r + ((operation == null) ? 0 : operation.hashCode());
        return r;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ArithmeticConstraint other = (ArithmeticConstraint) obj;
        return operation.equals(other.operation) && leftOperand.equals(other.leftOperand)
                && rightOperand.equals(other.rightOperand) && result.equals(other.result);
    }
}
