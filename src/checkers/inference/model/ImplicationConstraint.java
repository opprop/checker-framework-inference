package checkers.inference.model;

import org.checkerframework.dataflow.util.HashCodeUtils;
import org.checkerframework.javacutil.BugInCF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constraint that models implication logic. If all the assumptions are
 * satisfied, then conclusion should also be satisfied.
 * <p>
 * Suppose one needs to enforce this restriction: if {@code @A} is inferred as
 * on declaration of class {@code MyClass}, then every usage of class
 * {@code MyClass} needs to be inferred to {@code @A}.
 * {@link ImplicationConstraint} can express this restriction:
 * <p>
 * {@code @1 == @A -> @2 == @A}, in which {@code @1} is the slot inserted on the
 * class tree and {@code @2} is the slot that represents one usage of
 * {@code MyClass}.
 */
public class ImplicationConstraint extends Constraint {

    /**
     * An immutable list of {@link Constraint}s that are conjuncted together.
     */
    private final List<Constraint> assumptions;

    /**
     * A single {@link Constraint} that is implicated by the
     * {@link #assumptions}.
     */
    private final Constraint conclusion;

    public ImplicationConstraint(List<Constraint> assumptions,
            Constraint conclusion, AnnotationLocation location) {
        super(extractAllSlots(assumptions, conclusion), location);

        this.assumptions = Collections.unmodifiableList(assumptions);
        this.conclusion = conclusion;
    }

    private static List<Slot> extractAllSlots(List<Constraint> assumptions,
            Constraint conclusion) {
        List<Slot> slots = new ArrayList<>();
        for (Constraint a : assumptions) {
            slots.addAll(a.getSlots());
        }
        slots.addAll(conclusion.getSlots());
        return slots;
    }

    public static Constraint create(List<Constraint> assumptions,
            Constraint conclusion, AnnotationLocation currentLocation) {
        if (assumptions == null || conclusion == null) {
            throw new BugInCF(
                    "Adding implication constraint with null argument. assumptions: "
                            + assumptions + " conclusion: " + conclusion);
        }

        // Normalization cases:
        // 1) assumptions == empty ==> return conclusion
        // 2) any assumption == FALSE ==> return TRUE
        // 3) refinedAssumptions == empty ==> return conclusion
        // 4) refinedAssumptions != empty && conclusion == TRUE ==> return TRUE
        // 5) refinedAssumptions != empty && conclusion == FALSE ==> return
        // conjunction of refinedAssumptions
        // 6) refinedAssumptions != empty && conclusion != TRUE && conclusion !=
        // FALSE ==> CREATE_REAL_IMPLICATION_CONSTRAINT

        // 1) assumptions == empty ==> return conclusion
        // Optimization for trivial cases when there are no preconditions for
        // the conclusion to be true, meaning conclusion is a hard constraint
        // that must be satisfied.
        if (assumptions.isEmpty()) {
            return conclusion;
        }

        // Otherwise, assumptions list is not empty
        List<Constraint> refinedAssumptions = new ArrayList<>();
        // Iterate over assumptions: if any assumption is false, directly return
        // AlwaysTrueConstraint;
        // If any assumption is true, don't add it to the refined assumptions
        // list and continue the iteration.
        for (Constraint assumption : assumptions) {
            // 2) any assumption == FALSE ==> return TRUE
            if (assumption instanceof AlwaysFalseConstraint) {
                // assumption is false, the whole implication is true
                return AlwaysTrueConstraint.create();
            } else if (!(assumption instanceof AlwaysTrueConstraint)) {
                // current assumption is not statically known to true or false.
                refinedAssumptions.add(assumption);
            }
        }

        // 3) refinedAssumptions == empty ==> return conclusion
        if (refinedAssumptions.isEmpty()) {
            // This covers the case where original assumptions list is not empty
            // and every assumption is AlwaysTrueConstraint
            return conclusion;
        }

        // 4) refinedAssumptions != empty && conclusion == TRUE ==> return TRUE
        if (conclusion instanceof AlwaysTrueConstraint) {
            return AlwaysTrueConstraint.create();
        }

        // TODO: create a ConjunctionConstraint which gets de-sugared at
        // ConstraintManager
        // 5) refinedAssumptions != empty && conclusion == FALSE ==> return
        // conjunction of refinedAssumptions
        // if (conclusion instanceof AlwaysFalseConstraint) {
        // }

        // 6) refinedAssumptions != empty && conclusion != TRUE && conclusion !=
        // FALSE ==> CREATE_REAL_IMPLICATION_CONSTRAINT
        return new ImplicationConstraint(refinedAssumptions, conclusion,
                currentLocation);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    public List<Constraint> getAssumptions() {
        return assumptions;
    }

    public Constraint getConclusion() {
        return conclusion;
    }

    @Override
    public int hashCode() {
        int hash = HashCodeUtils.hash(conclusion);
        for (Constraint a : assumptions) {
            hash = HashCodeUtils.hash(hash, a);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ImplicationConstraint other = (ImplicationConstraint) obj;

        // TODO: How to compare the two lists of assumptions for equality?
        // aren't the assumptions really sets instead of lists?

        return conclusion.equals(other.conclusion);
    }
}
