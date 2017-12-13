package checkers.inference.model;

/**
 * Implemented by constraints between two variables. Make is provided to be able to make a copy of
 * the constraint without knowing the concrete base class. getLHS and getRHS are provided so that
 * users can either copy through a slot to a constraint made by make or substitute out a variable.
 * E.g.,
 *
 * // this call makes a copy of the constraint: make(getLHS(), getRHS())
 *
 * // this call modifies the lhs slot: make(mutate(getLHS()), getRHS())
 */
public interface BinaryConstraint {
    Slot getLHS();
    Slot getRHS();

    /**
     * Make a constraint that has the same class as this constraint but using the input slots.
     */
    Constraint make(final Slot lhs, final Slot rhs);
}
