package refval;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.javacutil.AnnotationBuilder;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstraintManager;
import refval.qual.RefVal;
import refval.qual.UnknownRefVal;

/**
 * Checker for RefVal type system.
 * 
 * @author jianchu
 *
 */
public class RefValChecker extends BaseInferrableChecker {
    public AnnotationMirror REFVAL, UNKNOWNREFVAL;

    @Override
    public void initChecker() {
        super.initChecker();
        setAnnotations();
    }

    protected void setAnnotations() {
        final Elements elements = processingEnv.getElementUtils();
        REFVAL = AnnotationBuilder.fromClass(elements, RefVal.class);
        UNKNOWNREFVAL = AnnotationBuilder.fromClass(elements, UnknownRefVal.class);
    }

    @Override
    public RefValVisitor createVisitor(InferenceChecker ichecker, BaseAnnotatedTypeFactory factory,
            boolean infer) {
        return new RefValVisitor(this, ichecker, factory, infer);
    }

    @Override
    public RefValAnnotatedTypeFactory createRealTypeFactory() {
        return new RefValAnnotatedTypeFactory(this);
    }

    @Override
    public RefValInferenceAnnotatedTypeFactory createInferenceATF(InferenceChecker inferenceChecker,
            InferrableChecker realChecker, BaseAnnotatedTypeFactory realTypeFactory,
            SlotManager slotManager, ConstraintManager constraintManager) {
        RefValInferenceAnnotatedTypeFactory refValInferenceTypeFactory = new RefValInferenceAnnotatedTypeFactory(
                inferenceChecker, realChecker.withCombineConstraints(), realTypeFactory, realChecker,
                slotManager, constraintManager);
        return refValInferenceTypeFactory;
    }
}