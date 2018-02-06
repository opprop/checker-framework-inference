package checkers.inference.model;

import java.util.Collections;
import org.checkerframework.javacutil.ErrorReporter;

/**
 * This "constraint" is the result of normalizing another constraint, where that constraint is
 * always true (evaluates to tautology). If a constraint is normalized to this constraint, then it
 * is not added to the set of constraints by the {@link ConstraintManager}. This class is
 * implemented as a singleton.
 *
 * @see {@link ConstraintManager}
 */
public class AlwaysTrueConstraint extends Constraint {

    private static AlwaysTrueConstraint singleton;

    private AlwaysTrueConstraint() {
        super(Collections.emptyList());
    }

    /** Creates/gets a singleton instance of the AlwaysTrueConstraint */
    protected static AlwaysTrueConstraint create() {
        if (singleton == null) {
            singleton = new AlwaysTrueConstraint();
        }
        return singleton;
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        ErrorReporter.errorAbort(
                "Attempting to serialize an " + AlwaysTrueConstraint.class.getCanonicalName()
                        + ". This constraint should never be serialized.");
        return null;
    }
}
