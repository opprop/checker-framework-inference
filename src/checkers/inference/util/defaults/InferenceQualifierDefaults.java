package checkers.inference.util.defaults;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.defaults.Default;
import org.checkerframework.framework.util.defaults.QualifierDefaults;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.qual.VarAnnot;

public class InferenceQualifierDefaults extends QualifierDefaults {

    public InferenceQualifierDefaults(Elements elements, AnnotatedTypeFactory atypeFactory) {
        super(elements, atypeFactory);
    }

    @Override
    protected DefaultApplierElement createDefaultApplierElement(AnnotatedTypeFactory atypeFactory,
            Element annotationScope, AnnotatedTypeMirror type, boolean applyToTypeVar) {
        return new InferenceDefaultApplierElement(atypeFactory, annotationScope, type, applyToTypeVar);
    }

    public class InferenceDefaultApplierElement extends DefaultApplierElement {

        private final SlotManager slotManager;

        public InferenceDefaultApplierElement(AnnotatedTypeFactory atypeFactory, Element scope,
                AnnotatedTypeMirror type, boolean applyToTypeVar) {
            super(atypeFactory, scope, type, applyToTypeVar);
            this.slotManager = InferenceMain.getInstance().getSlotManager();
        }

        @Override
        public void applyDefault(Default def) {
            this.location = def.location;
            ConstantSlot constantSlot = slotManager.createConstantSlot(def.anno);
            AnnotationMirror equivalentVarAnno = slotManager.getAnnotation(constantSlot);
            impl.visit(type, equivalentVarAnno);
        }

        @Override
        protected boolean shouldBeAnnotated(AnnotatedTypeMirror type, boolean applyToTypeVar) {
            return super.shouldBeAnnotated(type, applyToTypeVar) && !type.hasAnnotation(VarAnnot.class);
        }
    }

}
