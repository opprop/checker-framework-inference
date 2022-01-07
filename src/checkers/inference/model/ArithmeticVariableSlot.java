package checkers.inference.model;

import javax.lang.model.type.TypeKind;

/**
 * ArithmeticVariableSlot represent the result of an arithmetic operation between two other
 * {@link Slot}s. Note that this slot is serialized identically to a {@link Slot}.
 */
public class ArithmeticVariableSlot extends VariableSlot {
    /**
     * The value kind of the arithmetic operation, which indicates the value range.
     * The result depends on the wider operand. i.e.
     * If both sides are narrower than int => int
     * If one sides is long => long
     */
    private final TypeKind valueTypeKind;

    public ArithmeticVariableSlot(int id, AnnotationLocation location, TypeKind valueTypeKind) {
        super(id, location);
        this.valueTypeKind = valueTypeKind;
    }

    @Override
    public Kind getKind() {
        return Kind.ARITHMETIC_VARIABLE;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    /**
     * ArithmeticVariables should never be re-inserted into the source code.
     *
     * @return false
     */
    @Override
    public boolean isInsertable() {
        return false;
    }

    public TypeKind getValueTypeKind() {
        return valueTypeKind;
    }
}
