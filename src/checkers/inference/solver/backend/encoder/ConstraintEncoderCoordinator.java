package checkers.inference.solver.backend.encoder;

import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.BinaryConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.TernaryConstraint;
import checkers.inference.model.VariableSlot;
import checkers.inference.model.ViewpointAdaptationConstraint;
import checkers.inference.solver.backend.encoder.binary.BinaryConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ViewpointAdaptationConstraintEncoder;

/**
 * A coordinator class that has the coordinating logic how each encoder encodes its supported
 * constraint.
 * <p>
 * Dispatching example: this class dispatches the encoding of {@link BinaryConstraint} to the
 * corresponding encodeXXX_YYY() method in {@link BinaryConstraintEncoder} depending on the
 * {@link SlotSlotCombo} of {@link BinaryConstraint} that the encoder encodes. Similarly for
 * {@link TernaryConstraint}s to corresponding methods in the TernaryConstraint encoders
 * {@link ArithmeticConstraintEncoder} and {@link ViewpointAdaptationConstraintEncoder}.
 * <p>
 * Redirecting example: this class simply redirects encoding of {@link PreferenceConstraint} to
 * {@link PreferenceConstraintEncoder#encode(PreferenceConstraint)} method, as this kind of
 * constraint doesn't need the {@code SlotSlotCombo} information to encode it.
 *
 * @see BinaryConstraintEncoder
 * @see PreferenceConstraintEncoder
 * @see ExistentialConstraintEncoder
 * @see ArithmeticConstraintEncoder
 * @see ViewpointAdaptationConstraintEncoder
 */
public class ConstraintEncoderCoordinator {

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(BinaryConstraint constraint,
            BinaryConstraintEncoder<ConstraintEncodingT> encoder) {
        SlotSlotCombo combo = SlotSlotCombo.valueOf(constraint);
        switch (combo) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable((VariableSlot) constraint.getLHS(),
                        (VariableSlot) constraint.getRHS());
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant((VariableSlot) constraint.getLHS(),
                        (ConstantSlot) constraint.getRHS());
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable((ConstantSlot) constraint.getLHS(),
                        (VariableSlot) constraint.getRHS());
            case CONSTANT_CONSTANT:
                return encoder.encodeConstant_Constant((ConstantSlot) constraint.getLHS(),
                        (ConstantSlot) constraint.getRHS());
        }
        return null;
    }

    // TODO (jeff): see why BinaryConstraint interface is used, and where, see if it is removeable
    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ArithmeticConstraint constraint,
            ArithmeticConstraintEncoder<ConstraintEncodingT> encoder) {
        SlotSlotCombo combo = SlotSlotCombo.valueOf(constraint);
        switch (combo) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable(constraint.getOperation(),
                        (VariableSlot) constraint.getLeftOperand(),
                        (VariableSlot) constraint.getRightOperand(),
                        (VariableSlot) constraint.getResult());
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant(constraint.getOperation(),
                        (VariableSlot) constraint.getLeftOperand(),
                        (ConstantSlot) constraint.getRightOperand(),
                        (VariableSlot) constraint.getResult());
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable(constraint.getOperation(),
                        (ConstantSlot) constraint.getLeftOperand(),
                        (VariableSlot) constraint.getRightOperand(),
                        (VariableSlot) constraint.getResult());
            case CONSTANT_CONSTANT:
                return encoder.encodeConstant_Constant(constraint.getOperation(),
                        (ConstantSlot) constraint.getLeftOperand(),
                        (ConstantSlot) constraint.getRightOperand(),
                        (VariableSlot) constraint.getResult());
        }
        return null;
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ViewpointAdaptationConstraint constraint,
            ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> encoder) {
        SlotSlotCombo combo = SlotSlotCombo.valueOf(constraint);
        switch (combo) {
            case VARIABLE_VARIABLE:
                return encoder.encodeVariable_Variable((VariableSlot) constraint.getTarget(),
                        (VariableSlot) constraint.getDeclared(),
                        (VariableSlot) constraint.getResult());
            case VARIABLE_CONSTANT:
                return encoder.encodeVariable_Constant((VariableSlot) constraint.getTarget(),
                        (ConstantSlot) constraint.getDeclared(),
                        (VariableSlot) constraint.getResult());
            case CONSTANT_VARIABLE:
                return encoder.encodeConstant_Variable((ConstantSlot) constraint.getTarget(),
                        (VariableSlot) constraint.getDeclared(),
                        (VariableSlot) constraint.getResult());
            case CONSTANT_CONSTANT:
                return encoder.encodeConstant_Constant((ConstantSlot) constraint.getTarget(),
                        (ConstantSlot) constraint.getDeclared(),
                        (VariableSlot) constraint.getResult());
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
}
