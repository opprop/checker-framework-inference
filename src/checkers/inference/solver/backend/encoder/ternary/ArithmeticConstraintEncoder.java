package checkers.inference.solver.backend.encoder.ternary;

import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;

/**
 * Interface that defines operations to encode a
 * {@link checkers.inference.model.ArithmeticConstraint}. It has four methods depending on the
 * {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of the {@code leftOperand} and
 * {@code rightOperand} slots. The {@code result} slot is always a
 * {@link checkers.inference.model.TernaryVariableSlot}, which is essentially {@link VariableSlot}.
 * There's no methods in which {@code result} is a {@link ConstantSlot}.
 *
 * @see checkers.inference.model.ArithmeticConstraint
 * @see checkers.inference.solver.backend.encoder.ternary.TernaryConstraintEncoder
 */
public interface ArithmeticConstraintEncoder<ConstraintEncodingT> {
    ConstraintEncodingT encodeVariable_Variable(ArithmeticOperationKind operation,
            VariableSlot leftOperand, VariableSlot rightOperand, VariableSlot result);

    ConstraintEncodingT encodeVariable_Constant(ArithmeticOperationKind operation,
            VariableSlot leftOperand, ConstantSlot rightOperand, VariableSlot result);

    ConstraintEncodingT encodeConstant_Variable(ArithmeticOperationKind operation,
            ConstantSlot leftOperand, VariableSlot rightOperand, VariableSlot result);

    ConstraintEncodingT encodeConstant_Constant(ArithmeticOperationKind operation,
            ConstantSlot leftOperand, ConstantSlot rightOperand, VariableSlot result);
}
