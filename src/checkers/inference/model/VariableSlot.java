package checkers.inference.model;

import javax.lang.model.type.TypeMirror;

import org.checkerframework.javacutil.AnnotationUtils;

/**
 * VariableSlot is a Slot representing an undetermined value (i.e. a variable we are solving for).
 * After the Solver is run, each VariableSlot should have an assigned value which is then written
 * to the output Jaif file for later reinsertion into the original source code.
 *
 * Before the Solver is run, VariableSlots are represented by @VarAnnot( slot id ) annotations
 * on AnnotatedTypeMirrors.  When an AnnotatedTypeMirror is encountered in a position that would
 * generate constraints (e.g. either side of an assignment ), its @VarAnnots are converted into
 * VariableSlots which are then used in the generated constraints.
 *
 * E.g.  @VarAnnot(0) String s;
 * The above example implies that a VariableSlot with id 0 represents the possible annotations
 * on the declaration of s.
 *
 * Variable slot hold references to slots it is refined by, and slots it is merged to.
 *
 */
public class VariableSlot extends Slot {

    /** Actual type wrapped with this TypeMirror. */
    protected final TypeMirror actualType;

    /**
     * @param location Used to locate this variable in code, see @AnnotationLocation
     * @param id      Unique identifier for this variable
     * @param type the underlying type
     */
    public VariableSlot(AnnotationLocation location, int id, TypeMirror type) {
        super(id, location);
        this.actualType = type;
    }

    /**
     * @param location Used to locate this variable in code, see @AnnotationLocation
     * @param id      Unique identifier for this variable
     */
    public VariableSlot(int id, TypeMirror type) {
        super(id);
        this.actualType = type;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public Kind getKind() {
        return Kind.VARIABLE;
    }

    /**
     * Returns the underlying unannotated Java type, which this wraps.
     *
     * @return the underlying type
     */
    public TypeMirror getUnderlyingType() {
        return actualType;
    }

    /**
     * Should this VariableSlot be inserted back into the source code.
     */
    @Override
    public boolean isInsertable() {
        return true;
    }
}
