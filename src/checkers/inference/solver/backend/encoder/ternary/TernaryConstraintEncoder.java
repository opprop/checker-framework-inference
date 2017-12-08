package checkers.inference.solver.backend.encoder.ternary;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;

/**
 * Interface that defines operations to encode a {@link checkers.inference.model.TernaryConstraint}.
 * It has four methods depending on the
 * {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of the {@code first} and
 * {@code second} slots. The {@code third} slot is always a {@link VariableSlot}.
 *
 * <p>
 * The {@code third} slot is always a {@link checkers.inference.model.CombVariableSlot}, which is
 * essentially {@link VariableSlot}. There's no methods in which {@code result} is a
 * {@link ConstantSlot}.
 *
 * @see checkers.inference.model.TernaryConstraint
 * @see checkers.inference.solver.backend.encoder.SlotSlotCombo
 */
// TODO: rename CombVariableSlot to TernaryVariableSlot???
public interface TernaryConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encodeVariable_Variable(VariableSlot first, VariableSlot second,
            VariableSlot third);

    ConstraintEncodingT encodeVariable_Constant(VariableSlot first, ConstantSlot second,
            VariableSlot third);

    ConstraintEncodingT encodeConstant_Variable(ConstantSlot first, VariableSlot second,
            VariableSlot third);

    ConstraintEncodingT encodeConstant_Constant(ConstantSlot first, ConstantSlot second,
            VariableSlot third);
}
