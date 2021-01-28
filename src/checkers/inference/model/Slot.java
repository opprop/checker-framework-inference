package checkers.inference.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

/**
 * Slots represent logical variables over which Constraints are generated.
 *
 * Each slot is attached to a code location that can hold an annotation OR has an intrinsic meaning
 * within type-systems. E.g: an int literal is always NonNull but can't hold an annotation,
 * nonetheless, we generate a ConstantSlot representing the literal.
 */
public abstract class Slot implements Comparable<Slot> {

    /**
     * Used to locate this Slot in source code. ASTRecords are written to Jaif files along with the
     * Annotation determined for this slot by the Solver.
     */
    private AnnotationLocation location;

    /**
     * Uniquely identifies this Slot.  id's are monotonically increasing in value by the order they
     * are generated
     */
    private final int id;

    /**
     * Should this VariableSlot be inserted back into the source code.
     * This should be false for types have have an implicit annotation
     * and slots for pre-annotated code.
     */
    private boolean insertable;

    /**
     * Create a Slot with the given annotation location.
     *
     * @param id Unique identifier for this variable
     * @param insertable whether this variable is insertable into source code or not
     * @param location an AnnotationLocation for which the slot is attached to
     */
    public Slot(int id, AnnotationLocation location) {
        this.id = id;
        this.insertable = false;
        this.location = location;
    }

    /**
     * Create a slot with a default location of
     * {@link AnnotationLocation#MISSING_LOCATION}.
     *
     * @param id Unique identifier for this variable
     * @param insertable whether this variable is insertable into source code or not
     */
    public Slot(int id) {
        this(id, AnnotationLocation.MISSING_LOCATION);
    }

     // Slots this variable has been merged to.
    private final Set<LubVariableSlot> mergedToSlots = new HashSet<>();

    // Refinement variables that refine this slot.
    private final Set<RefinementVariableSlot> refinedToSlots = new HashSet<>();

    public abstract <S, T> S serialize(Serializer<S, T> serializer);

    public int getId() {
        return id;
    }

    public AnnotationLocation getLocation() {
        return location;
    }

    public void setLocation(AnnotationLocation location) {
        this.location = location;
    }

    /**
     * Should this VariableSlot be inserted back into the source code.
     * This should be false for types have have an implicit annotation
     * and slots for pre-annotated code.
     */
    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public abstract Kind getKind();

    public enum Kind {
        VARIABLE,
        CONSTANT,
        REFINEMENT_VARIABLE,
        EXISTENTIAL_VARIABLE,
        COMB_VARIABLE,
        ARITHMETIC_VARIABLE,
        COMPARISON_VARIABLE,
        LUB_VARIABLE
    }

    public boolean isVariable() {
        return !isConstant();
    }

    public boolean isConstant() {
        return this instanceof ConstantSlot;
    }

    public boolean isMergedTo(Slot other) {
        for (Slot mergedTo: mergedToSlots) {
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

    public Set<LubVariableSlot> getMergedToSlots() {
        return Collections.unmodifiableSet(mergedToSlots);
    }

    public void addMergedToSlot(LubVariableSlot mergedSlot) {
        this.mergedToSlots.add(mergedSlot);
    }

    public Set<RefinementVariableSlot> getRefinedToSlots() {
        return refinedToSlots;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + id + ")";
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
}
