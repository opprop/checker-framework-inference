package checkers.inference.util;

import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;
import checkers.inference.model.serialization.ToStringSerializer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mier on 04/08/17.
 * Transitively prints all non-constant slots in a constraint. Each slot is only
 * printed once.
 */
public final class SlotsPrinter implements Serializer<Void, Void> {

    /**Delegatee that serializes slots to string representation.*/
    private final ToStringSerializer toStringSerializer;
    /**Stores already-printed slots so they won't be printed again.*/
    private final Set<Slot> printedSlots;


    public SlotsPrinter(final ToStringSerializer toStringSerializer) {
        this.toStringSerializer = toStringSerializer;
        printedSlots = new HashSet<>();
    }

    private void printSlotIfNotPrinted(Slot slot) {
        if (printedSlots.add(slot) && !(slot instanceof ConstantSlot)) {
            System.out.println("\t" + slot.serialize(toStringSerializer) + " \n\t    " + slot.getLocation().toString() + "\n");
        }
    }

    @Override
    public Void serialize(SubtypeConstraint constraint) {
        constraint.getSubtype().serialize(this);
        constraint.getSupertype().serialize(this);
        return null;
    }

    @Override
    public Void serialize(EqualityConstraint constraint) {
        constraint.getFirst().serialize(this);
        constraint.getSecond().serialize(this);
        return null;
    }

    @Override
    public Void serialize(ExistentialConstraint constraint) {
        constraint.getPotentialVariable().serialize(this);
        return null;
    }

    @Override
    public Void serialize(InequalityConstraint constraint) {
        constraint.getFirst().serialize(this);
        constraint.getSecond().serialize(this);
        return null;
    }

    @Override
    public Void serialize(ComparableConstraint comparableConstraint) {
        comparableConstraint.getFirst().serialize(this);
        comparableConstraint.getSecond().serialize(this);
        return null;
    }

    @Override
    public Void serialize(CombineConstraint combineConstraint) {
        combineConstraint.getResult().serialize(this);
        combineConstraint.getTarget().serialize(this);
        combineConstraint.getDeclared().serialize(this);
        return null;
    }

    @Override
    public Void serialize(PreferenceConstraint preferenceConstraint) {
        preferenceConstraint.getVariable().serialize(this);
        return null;
    }

    @Override
    public Void serialize(VariableSlot slot) {
        printSlotIfNotPrinted(slot);
        return null;
    }

    @Override
    public Void serialize(ConstantSlot slot) {
        return null;
    }

    @Override
    public Void serialize(ExistentialVariableSlot slot) {
        slot.getPotentialSlot().serialize(this);
        slot.getAlternativeSlot().serialize(this);
        printSlotIfNotPrinted(slot);
        return null;
    }

    @Override
    public Void serialize(RefinementVariableSlot slot) {
        slot.getRefined().serialize(this);
        printSlotIfNotPrinted(slot);
        return null;
    }

    @Override
    public Void serialize(CombVariableSlot slot) {
        slot.getFirst().serialize(this);
        slot.getSecond().serialize(this);
        printSlotIfNotPrinted(slot);
        return null;
    }

}
