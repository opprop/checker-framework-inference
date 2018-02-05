package checkers.inference.model;

import java.util.Collections;

/**
 * This "constraint" is the result of normalizing another constraint, where that constraint is
 * always false (evaluates to contradiction). If a constraint is normalized to this constraint, then
 * an error message may be issued by the {@link ConstraintManager}.
 *
 * @see {@link ConstraintManager}
 */
public class AlwaysFalseConstraint extends Constraint {

    private AlwaysFalseConstraint() {
        super(Collections.emptyList());
    }

    public static Constraint create() {
        return new AlwaysFalseConstraint();
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return null;
    }
}
