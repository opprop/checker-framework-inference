package checkers.inference.solver.backend.encoder.ternary;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;

/**
 * Interface that defines operations to encode a
 * {@link checkers.inference.model.ViewpointAdaptationConstraint}. It has four methods depending on
 * the {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of {@code target} and {@code
 * declared} slots.
 *
 * <p>
 * {@code result} is always {@link checkers.inference.model.TernaryVariableSlot}, which is
 * essentially {@link VariableSlot}, whose {@link VariableSlot#id} is the only interesting knowledge
 * in encoding phase. There's no methods in which {@code result} is a {@link ConstantSlot}.
 *
 * @see checkers.inference.model.ViewpointAdaptationConstraint
 * @see checkers.inference.solver.backend.encoder.SlotSlotCombo
 */
public interface ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> {
    ConstraintEncodingT encodeVariable_Variable(VariableSlot target, VariableSlot decl,
            VariableSlot result);

    ConstraintEncodingT encodeVariable_Constant(VariableSlot target, ConstantSlot decl,
            VariableSlot result);

    ConstraintEncodingT encodeConstant_Variable(ConstantSlot target, VariableSlot decl,
            VariableSlot result);

    ConstraintEncodingT encodeConstant_Constant(ConstantSlot target, ConstantSlot decl,
            VariableSlot result);
}
