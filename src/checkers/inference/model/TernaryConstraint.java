package checkers.inference.model;

/**
 * This interface is implemented by constraints between three variables. Make is provided to be able
 * to make a copy of the constraint without knowing the concrete base class. {@link #getFirst()},
 * {@link #getSecond()}, and {@link #getThird()} are provided so that users can either copy through
 * a slot to a constraint made by make or substitute out a variable. E.g.,
 *
 * <p>
 * This call makes a copy of the constraint:
 * {@code make(getFirst(), getSecond(), getThird(), getLocation()}
 *
 * <p>
 * This call modifies the first slot:
 * {@code make(mutate(getFirst()), getSecond(), getThird(), getLocation())}
 * 
 * <p>
 * Note: {@code getLocation()} is provided by {@link Constraint}
 */
public interface TernaryConstraint {
    Slot getFirst();
    Slot getSecond();
    Slot getThird();

    /**
     * Make a constraint that has the same class as this constraint but using the input slots and
     * location.
     */
    Constraint make(final Slot first, final Slot second, final Slot third,
            final AnnotationLocation location);
}
