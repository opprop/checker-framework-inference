package checkers.inference.model;

import org.checkerframework.javacutil.BugInCF;

import java.util.ArrayList;
import java.util.List;

/**
 * Constraint that models implication logic. If all the assumptions are satisfied, then
 * conclusion should also be satisfied.
 *
 * This is intended to express the restrictions between solutions for {@link Slot}s.
 *
 * Suppose one needs to enforce this restriction(just an example. Doesn't indicate universal type rule):
 * if {@code @A} is inferred as on declaration of class {@code MyClass}, then every usage of class
 * {@code MyClass} needs to be inferred to {@code @A}, but to nothing else. {@link ImplicationConstraint}
 * can express this restriction:
 * <p>
 * {@code @1 == @A -> @2 == @A}, in which {@code @1} is the slot inserted on the class tree and
 * {@code @2} is the slot that represents one usage of {@code MyClass}.
 */
public class ImplicationConstraint extends Constraint {

    /**
     * A list of {@link Constraint}s that are conjuncted together.
     */
    private final List<Constraint> assumptions;

    /**
     * A single {@link Constraint} that is implicated by the {@link #assumptions}.
     */
    private final Constraint conclusion;

    public ImplicationConstraint(
            List<Constraint> assumptions, Constraint conclusion, AnnotationLocation location) {
        super(extractAllSlots(assumptions, conclusion), location);

        this.assumptions = assumptions;
        this.conclusion = conclusion;
    }

    private static List<Slot> extractAllSlots(List<Constraint> assumptions, Constraint conclusion) {
        List<Slot> slots = new ArrayList<>();
        for(Constraint a : assumptions) {
            slots.addAll(a.getSlots());
        }
        slots.addAll(conclusion.getSlots());
        return slots;
    }

    public List<Constraint> getAssumptions() {
        return assumptions;
    }

    public Constraint getConclusion() {
        return conclusion;
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for(Constraint a : assumptions) {
            result = result * prime + a.hashCode();
        }
        result = result * prime + conclusion.hashCode();
        return result;
    }

    public static Constraint create(List<Constraint> assumptions, Constraint conclusion, AnnotationLocation currentLocation) {
        if (assumptions == null || conclusion == null) {
            throw new BugInCF("Adding implication constraint with null argument. assumptions: "
                    + assumptions + " conclusion: " + conclusion);
        }

        if (assumptions.isEmpty()) {
            // Optimization for trivial cases when there are no preconditions for the conclusion to be true
            // , meaning conclusion is a hard Constraint that must be satisfied.
            return conclusion;
        }

        // Otherwise, assumptions list is not empty
        List<Constraint> refinedAssumptions = new ArrayList<>();
        // Iterate over assumptions: if any assumption is false, directly return AlwaysTrueConstraint;
        // if any assumption is true, don't add it to the refined assumptions list and continue the iteration.
        for (Constraint assumption : assumptions) {
            if (assumption instanceof AlwaysFalseConstraint) {
                // assumption is false, the whole implication is true
                return AlwaysTrueConstraint.create();
            } else if (assumption instanceof AlwaysTrueConstraint) {
                continue;
            } else {
                // current assumption is not statically known to true or false.
                refinedAssumptions.add(assumption);
            }
        }

        if (refinedAssumptions.isEmpty()) {
            // This covers the case where original assumptions list is not empty and every assummption is AlwaysTrueConstraint
            return conclusion;
        } else {
            return new ImplicationConstraint(refinedAssumptions, conclusion, currentLocation);
        }
    }
}
