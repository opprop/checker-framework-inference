package checkers.inference.solver.backend.encoder.ternary;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;

/**
 * Interface that defines operations to encode a {@link checkers.inference.model.TernaryConstraint}.
 * It has four methods depending on the
 * {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of the {@code leftOperand} and
 * {@code rightOperand} slots. The {@code result} slot is always a {@link VariableSlot}.
 *
 * <p>
 * The {@code result} slot is always a {@link checkers.inference.model.TernaryVariableSlot}, which
 * is essentially {@link VariableSlot}. There's no methods in which {@code result} is a
 * {@link ConstantSlot}.
 *
 * @see checkers.inference.model.TernaryConstraint
 * @see checkers.inference.solver.backend.encoder.SlotSlotCombo
 */
public interface TernaryConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encodeVariable_Variable(VariableSlot leftOperand, VariableSlot rightOperand,
            VariableSlot result);

    ConstraintEncodingT encodeVariable_Constant(VariableSlot leftOperand, ConstantSlot rightOperand,
            VariableSlot result);

    ConstraintEncodingT encodeConstant_Variable(ConstantSlot leftOperand, VariableSlot rightOperand,
            VariableSlot result);

    ConstraintEncodingT encodeConstant_Constant(ConstantSlot leftOperand, ConstantSlot rightOperand,
            VariableSlot result);
}
