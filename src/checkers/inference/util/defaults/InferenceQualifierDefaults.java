package checkers.inference.util.defaults;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.defaults.Default;
import org.checkerframework.framework.util.defaults.QualifierDefaults;

import checkers.inference.qual.VarAnnot;
import checkers.inference.util.ConstantToVariableAnnotator;

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

        public InferenceDefaultApplierElement(AnnotatedTypeFactory atypeFactory, Element scope,
                AnnotatedTypeMirror type, boolean applyToTypeVar) {
            super(atypeFactory, scope, type, applyToTypeVar);
        }

        @Override
        public void applyDefault(Default def) {
            this.location = def.location;
            AnnotationMirror equivalentVarAnno = ConstantToVariableAnnotator.createEquivalentVarAnno(def.anno);
            impl.visit(type, equivalentVarAnno);
        }

        @Override
        protected boolean shouldBeAnnotated(AnnotatedTypeMirror type, boolean applyToTypeVar) {
            return super.shouldBeAnnotated(type, applyToTypeVar) && !type.hasAnnotation(VarAnnot.class);
        }
    }

}
