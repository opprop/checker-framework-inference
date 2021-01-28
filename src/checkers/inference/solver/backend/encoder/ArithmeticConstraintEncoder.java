package checkers.inference.solver.backend.encoder;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;

/**
 * Interface that defines operations to encode a
 * {@link checkers.inference.model.ArithmeticConstraint}. It has four methods depending on the
 * {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of the {@code leftOperand} and
 * {@code rightOperand} slots.
 *
 * @see checkers.inference.model.ArithmeticConstraint
 */
public interface ArithmeticConstraintEncoder<ConstraintEncodingT> {
    ConstraintEncodingT encodeVariable_Variable(ArithmeticOperationKind operation,
            Slot leftOperand, Slot rightOperand, ArithmeticVariableSlot result);

    ConstraintEncodingT encodeVariable_Constant(ArithmeticOperationKind operation,
            Slot leftOperand, ConstantSlot rightOperand, ArithmeticVariableSlot result);

    ConstraintEncodingT encodeConstant_Variable(ArithmeticOperationKind operation,
            ConstantSlot leftOperand, Slot rightOperand, ArithmeticVariableSlot result);

    ConstraintEncodingT encodeConstant_Constant(ArithmeticOperationKind operation,
            ConstantSlot leftOperand, ConstantSlot rightOperand, ArithmeticVariableSlot result);
}
