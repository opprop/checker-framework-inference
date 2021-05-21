package checkers.inference.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Slots represent constraint variables that represent either
 * (1) the qualifiers of the real type system {@link ConstantSlot} or
 * (2) placeholders for which a solution needs to be inferred {@link VariableSlot}.
 *
 * Each Slot has a unique identification number.
 *
 * Slots are represented by {@code @VarAnnot( slot id )} annotations in AnnotatedTypeMirrors.
 * The {@link checkers.inference.VariableAnnotator} generates the Slots for source code.
 *
 * A slot maintains the set of {@link LubVariableSlot}s of least-upper bound computations it is
 * involved in.
 *
 */
public abstract class Slot implements Comparable<Slot> {
    /**
     * Uniquely identifies this Slot.  id's are monotonically increasing in value by the order they
     * are generated
     */
    protected final int id;

    /**
     * Slots this variable has been merged to.
     */
    private final Set<LubVariableSlot> mergedToSlots = new HashSet<>();

    public Slot(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract Kind getKind();

    public abstract boolean isInsertable();

    public abstract boolean isVariable();

    public abstract <S, T> S serialize(Serializer<S, T> serializer);

    public Set<LubVariableSlot> getMergedToSlots() {
        return Collections.unmodifiableSet(mergedToSlots);
    }

    public void addMergedToSlot(LubVariableSlot mergedSlot) {
        this.mergedToSlots.add(mergedSlot);
    }

    public boolean isMergedTo(Slot other) {
        for (LubVariableSlot mergedTo: mergedToSlots) {
            if (mergedTo.equals(other)) {
                return true;
            } else {
                if (mergedTo.isMergedTo(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Slot other = (Slot) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int compareTo(Slot other) {
        return Integer.compare(id, other.id);
    }

    public enum Kind {
        VARIABLE,
        CONSTANT,
        REFINEMENT_VARIABLE,
        EXISTENTIAL_VARIABLE,
        COMB_VARIABLE,
        ARITHMETIC_VARIABLE,
        LUB_VARIABLE
    }
}
