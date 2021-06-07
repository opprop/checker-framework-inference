package checkers.inference.solver.backend.encoder;

import org.checkerframework.javacutil.BugInCF;
import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.BinaryConstraint;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ImplicationConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.binary.BinaryConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.implication.ImplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;

/**
 * A coordinator class that has the coordinating logic how each encoder encodes its supported
 * constraint.
 * <p>
 * Dispatching example: this class dispatches the encoding of {@link BinaryConstraint} to the
 * corresponding encodeXXX_YYY() method in {@link BinaryConstraintEncoder} depending on the
 * {@link SlotSlotCombo} of {@link BinaryConstraint} that the encoder encodes.
 * <p>
 * Redirecting example: this class simply redirects encoding of {@link PreferenceConstraint} to
 * {@link PreferenceConstraintEncoder#encode(PreferenceConstraint)} method, as this kind of
 * constraint doesn't need the {@code SlotSlotCombo} information to encode it.
 *
 * @see BinaryConstraintEncoder
 * @see CombineConstraintEncoder
 * @see PreferenceConstraintEncoder
 * @see ExistentialConstraintEncoder
 */
public class ConstraintEncoderCoordinator {

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(BinaryConstraint constraint,
            BinaryConstraintEncoder<ConstraintEncodingT> encoder) {
        Slot first = constraint.getFirst();
        Slot second = constraint.getSecond();
        switch (SlotSlotCombo.valueOf(first, second)) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable((VariableSlot) first,
                        (VariableSlot) second);
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant((VariableSlot) first,
                        (ConstantSlot) second);
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable((ConstantSlot) first,
                        (VariableSlot) second);
            case CONSTANT_CONSTANT:
                throw new BugInCF("Attempting to encode a constant-constant combination "
                        + "for a binary constraint. This should be normalized to "
                        + "either AlwaysTrueConstraint or AlwaysFalseConstraint.");
            default:
                throw new BugInCF("Unsupported SlotSlotCombo enum.");
        }
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(CombineConstraint constraint,
            CombineConstraintEncoder<ConstraintEncodingT> encoder) {
        Slot target = constraint.getTarget();
        Slot declared = constraint.getDeclared();
        switch (SlotSlotCombo.valueOf(target, declared)) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable((VariableSlot) target,
                        (VariableSlot) declared,
                        constraint.getResult());
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant((VariableSlot) target,
                        (ConstantSlot) declared,
                        constraint.getResult());
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable((ConstantSlot) target,
                        (VariableSlot) declared,
                        constraint.getResult());
            case CONSTANT_CONSTANT:
                return encoder.encodeConstant_Constant((ConstantSlot) target,
                        (ConstantSlot) declared,
                        constraint.getResult());
            default:
                throw new BugInCF("Unsupported SlotSlotCombo enum.");
        }
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ArithmeticConstraint constraint,
            ArithmeticConstraintEncoder<ConstraintEncodingT> encoder) {
        Slot leftOperand = constraint.getLeftOperand();
        Slot rightOperand = constraint.getRightOperand();
        switch (SlotSlotCombo.valueOf(leftOperand, rightOperand)) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable(constraint.getOperation(),
                        (VariableSlot) leftOperand,
                        (VariableSlot) rightOperand, constraint.getResult());
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant(constraint.getOperation(),
                        (VariableSlot) leftOperand,
                        (ConstantSlot) rightOperand, constraint.getResult());
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable(constraint.getOperation(),
                        (ConstantSlot) leftOperand,
                        (VariableSlot) rightOperand, constraint.getResult());
            case CONSTANT_CONSTANT:
                return encoder.encodeConstant_Constant(constraint.getOperation(),
                        (ConstantSlot) leftOperand,
                        (ConstantSlot) rightOperand, constraint.getResult());
        }
        return null;
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(
            PreferenceConstraint constraint,
            PreferenceConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(
            ExistentialConstraint constraint,
            ExistentialConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(ImplicationConstraint constraint, ImplicationConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }
}
