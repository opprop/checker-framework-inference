package checkers.inference.model;

import checkers.inference.InferenceOptions;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

/**
 * SourceVariableSlot is a VariableSlot representing a type use in the source code with undetermined value.
 */
public class SourceVariableSlot extends VariableSlot {

    /** The actual type of the type use */
    protected final TypeMirror actualType;

    /**
     * The default annotation for this slot from the real type factory.
     * This field is nullable because we find the default annotation only
     * if {@link InferenceOptions#makeDefaultsExplicit} is true.
     */
    protected final @Nullable AnnotationMirror defaultAnnotation;

    /**
     * Should this slot be inserted back into the source code.
     * This should be false for types that have an implicit annotation
     * and slots for pre-annotated code.
     */
    private boolean insertable;

    /**
     * @param location used to locate this variable in code, see @AnnotationLocation
     * @param id unique identifier for this variable
     * @param type the underlying type
     * @param defaultAnnotation the default annotation (solution) for this slot, which can be null when
     *                          {@link InferenceOptions#makeDefaultsExplicit} returns true
     * @param insertable indicates whether this slot should be inserted back into the source code
     */
    public SourceVariableSlot(
            int id,
            AnnotationLocation location,
            TypeMirror type,
            @Nullable AnnotationMirror defaultAnnotation,
            boolean insertable
    ) {
        super(id, location);
        this.actualType = type;
        this.defaultAnnotation = defaultAnnotation;
        this.insertable = insertable;
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
        return insertable;
    }

    /**
     * This method is not encouraged to use, since whether a specific {@code SourceVariableSlot}
     * is insertable should be determined at creation depending on the type use location, while
     * sometime it's more convenient to set this flag of a {@code SourceVariableSlot} when it's
     * use.
     * TODO: determine whether the slot is insertable at creation and remove this method
     * @param insertable whether this slot should be inserted back into the source code
     */
    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    public @Nullable AnnotationMirror getDefaultAnnotation() {
        return defaultAnnotation;
    }
}
