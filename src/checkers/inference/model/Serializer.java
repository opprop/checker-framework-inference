package checkers.inference.model;

/**
 * Interface for serializing constraints and variables.
 *
 * Serialization will occur for all variables and constraints either
 * before or instead of Constraint solving.
 *
 * This allows us to avoid re-generating constraints for a piece of
 * source code every time we wish to solve (for instance when a new
 * solver is written or an existing one is modified).
 *
 * Type parameters S and T are used to adapt the return type of the
 * XXXSlot visitor methods (S) and the XXXConstraint visitor methods
 * (T).
 * Implementing classes can use the same or different types for these
 * type parameters.
 */
public interface Serializer<SlotEncodingT, ConstraintEncodingT> {

    ConstraintEncodingT serialize(SubtypeConstraint constraint);

    ConstraintEncodingT serialize(EqualityConstraint constraint);

    ConstraintEncodingT serialize(InequalityConstraint constraint);

    ConstraintEncodingT serialize(ComparableConstraint comparableConstraint);

    ConstraintEncodingT serialize(PreferenceConstraint preferenceConstraint);

    ConstraintEncodingT serialize(ExistentialConstraint constraint);

    ConstraintEncodingT serialize(ViewpointAdaptationConstraint viewpointAdaptationConstraint);

    ConstraintEncodingT serialize(AdditionConstraint addConstraint);

    ConstraintEncodingT serialize(SubtractionConstraint subConstraint);

    ConstraintEncodingT serialize(MultiplicationConstraint mulConstraint);

    ConstraintEncodingT serialize(DivisionConstraint divConstraint);

    ConstraintEncodingT serialize(ModulusConstraint modConstraint);

    SlotEncodingT serialize(VariableSlot slot);

    SlotEncodingT serialize(ConstantSlot slot);

    SlotEncodingT serialize(ExistentialVariableSlot slot);

    SlotEncodingT serialize(RefinementVariableSlot slot);

    SlotEncodingT serialize(TernaryVariableSlot slot);
}
