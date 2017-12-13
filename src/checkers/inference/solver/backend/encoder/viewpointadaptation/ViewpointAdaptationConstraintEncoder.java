package checkers.inference.solver.backend.encoder.viewpointadaptation;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ternary.TernaryConstraintEncoder;

/**
 * Interface that defines operations to encode a
 * {@link checkers.inference.model.ViewpointAdaptationConstraint}. It has four methods depending on
 * the {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of {@code target} and {@code
 * declared} slots.
 *
 * <p>
 * {@code result} is always {@link checkers.inference.model.TernaryVariableSlot}, which is
 * essentially {@link VariableSlot}, whose {@link VariableSlot#id} is the only interesting knowledge
 * in encoding phase. Therefore there don't exist methods in which {@code result} is
 * {@link ConstantSlot}.
 *
 * @see checkers.inference.model.ViewpointAdaptationConstraint
 * @see checkers.inference.solver.backend.encoder.SlotSlotCombo
 */
public interface ViewpointAdaptationConstraintEncoder<ConstraintEncodingT>
        extends TernaryConstraintEncoder<ConstraintEncodingT> {
}
