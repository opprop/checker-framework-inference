package checkers.inference.model;

import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;

import checkers.inference.qual.VarAnnot;

import javax.lang.model.element.AnnotationMirror;

/**
 * Represents a real qualifier of the type system.
 *
 * Constant slots are used for elements with fixed types, e.g. literals.
 */
public class ConstantSlot extends Slot {

    /**
     * The annotation in the real type system that this slot is equivalent to
     */
    private final AnnotationMirror value;

    /**
     *
     * @param value   The actual AnnotationMirror that this ConstantSlot represents.  This AnnotationMirror should
     *                be valid within the type system for which we are inferring values.
     * @param id      Exactly like a variable id, this will uniquely identify this constant in the entirey of the
     *                program
     *
     * The location for slots constructed using this constructor will be AnnotationLocation.MISSING_LOCATION
     */
    public ConstantSlot(int id, AnnotationMirror value) {
        super(id);
        checkValue(value);
        this.value = value;
    }

    private void checkValue(AnnotationMirror value) {
        if (AnnotationUtils.areSameByClass(value, VarAnnot.class)) {
            throw new BugInCF("Invalid attempt to create a ConstantSlot with VarAnnot as value: " + value);
        }
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    /**
     * @return The "real" annotation that this ConstantSlot is equal to
     */
    public AnnotationMirror getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.toString().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConstantSlot other = (ConstantSlot) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!AnnotationUtils.areSame(value,other.value))
            return false;
        return true;
    }
}
