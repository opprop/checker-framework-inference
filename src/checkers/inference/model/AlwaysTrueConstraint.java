package checkers.inference.model;

import java.util.Collections;

/**
 * This "constraint" is the result of normalizing another constraint, where that constraint is
 * always true (evaluates to tautology). If a constraint is normalized to this constraint, then it
 * is not added to the set of constraints by the {@link ConstraintManager}.
 * 
 * @see {@link ConstraintManager}
 */
public class AlwaysTrueConstraint extends Constraint {

    private AlwaysTrueConstraint() {
        super(Collections.emptyList());
    }

    public static Constraint create() {
        return new AlwaysTrueConstraint();
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return null;
    }
}
