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

// TODO: merge the 5 subclasses into here

public abstract class ArithmeticConstraint extends Constraint implements TernaryConstraint {

    public enum ArithmeticOperationKind {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULUS
    }

    private final Slot leftOperand;
    private final Slot rightOperand;
    private final Slot result;

    protected ArithmeticConstraint(Slot leftOperand, Slot rightOperand, Slot result,
            AnnotationLocation location) {
        super(Arrays.asList(leftOperand, rightOperand, result), location);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.result = result;
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
    public int hashCode() {
        int hc = 1;
        hc += ((leftOperand == null) ? 0 : leftOperand.hashCode());
        hc += ((rightOperand == null) ? 0 : rightOperand.hashCode());
        hc += ((result == null) ? 0 : result.hashCode());
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArithmeticConstraint other = (ArithmeticConstraint) obj;
        return leftOperand.equals(other.leftOperand) && rightOperand.equals(other.rightOperand)
                && result.equals(other.result);
    }
}
