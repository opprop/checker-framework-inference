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

/**
 * Apply default qualifiers in inference mode.
 *
 * In inference mode, unchecked bytecode needs default qualifiers.
 * To build constraints, these default qualifiers should be VarAnnots.
 * The super class {@code QualifierDefaults} would determine real
 * qualifiers for each type use location, and this class would replace
 * those real qualifiers by equivalent VarAnnots, and apply these
 * VarAnnots as defaults to a given type only if this type has not been
 * annotated with a VarAnnot.
 *
 * @see org.checkerframework.framework.util.defaults.QualifierDefaults
 *
 */
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
