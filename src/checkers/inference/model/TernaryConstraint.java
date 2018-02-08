package checkers.inference.model;

/**
 * This interface is implemented by constraints between three variables.
 */
public interface TernaryConstraint {
    Slot getLeftOperand();

    Slot getRightOperand();

    Slot getResult();
}
