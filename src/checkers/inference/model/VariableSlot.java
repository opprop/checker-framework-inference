package checkers.inference.model;


import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.Set;

/**
 * VariableSlot is a Slot representing an undetermined value (i.e. a variable we are solving for).
 * After the Solver is run, each VariableSlot has an assigned value which is then written
 * to the output Jaif file for later insertion into the original source code.
 *
 * The {@link checkers.inference.InferenceVisitor} and other code can convert between VarAnnots in
 * AnnotatedTypeMirrors and VariableSlots, which are then used to generate constraints.
 *
 * E.g.  @VarAnnot(0) String s;
 *
 * The above example implies that a VariableSlot with id 0 represents the possible annotations
 * on the declaration of s.
 *
 * Every VariableSlot has an {@link AnnotationLocation} and the {@link RefinementVariableSlot}s
 * it is refined by.
 *
 */
public abstract class VariableSlot extends Slot {

    /**
     * Used to locate this Slot in source code. {@code AnnotationLocation}s are written to Jaif files
     * along with the annotations determined for this slot by the Solver.
     */
    private AnnotationLocation location;

    /** Refinement variables that refine this slot. */
    private final Set<RefinementVariableSlot> refinedToSlots = new HashSet<>();

    /**
     * Create a Slot with the given annotation location.
     *
     * @param id Unique identifier for this variable
     * @param location an AnnotationLocation for which the slot is attached to
     */
    public VariableSlot(int id, AnnotationLocation location) {
        super(id);
        this.location = location;
    }

    public AnnotationLocation getLocation() {
        return location;
    }

    // TODO: remove this method and make location final.
    public void setLocation(AnnotationLocation location) {
        this.location = location;
    }

    public Set<RefinementVariableSlot> getRefinedToSlots() {
        return refinedToSlots;
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + id + ")";
    }
}
