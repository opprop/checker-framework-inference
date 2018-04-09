package checkers.inference.util;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.Slot;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.ViewpointAdapter;

import javax.lang.model.element.AnnotationMirror;

public class InferenceViewpointAdapter extends ViewpointAdapter<Slot>{

    private final SlotManager slotManager = InferenceMain.getInstance().getSlotManager();
    private final ConstraintManager constraintManager = InferenceMain.getInstance().getConstraintManager();

    @Override
    protected Slot extractModifier(AnnotatedTypeMirror atm, AnnotatedTypeFactory atypeFactory) {
        return slotManager.getVariableSlot(atm);
    }

    @Override
    protected AnnotationMirror extractAnnotationMirror(Slot slot) {
        return slotManager.getAnnotation(slot);
    }

    @Override
    protected Slot combineModifierWithModifier(Slot recvModifier, Slot declModifier, AnnotatedTypeFactory atypeFactory) {
        Slot combVariableSlot = slotManager.createCombVariableSlot(recvModifier, declModifier);
        constraintManager.addCombineConstraint(recvModifier, declModifier, combVariableSlot);
        return combVariableSlot;
    }
}
