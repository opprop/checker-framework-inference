package checkers.inference;


import com.sun.source.tree.Tree;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeValidator;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

/**
 * A visitor to validate the types in a tree.
 */
public class InferenceValidator extends BaseTypeValidator {

    /**
     * Indicates whether the validator is in inference mode or not.
     *
     * This field is intended to make implementations of subclasses easier.
     * Instead of querying the {@link InferenceVisitor#infer}, subclasses can
     * directly query this field to decide whether to generate constraints or
     * perform typechecking.
     */
    protected boolean infer;

    public InferenceValidator(BaseTypeChecker checker,
            InferenceVisitor<?, ?> visitor,
            AnnotatedTypeFactory atypeFactory) {
        super(checker, visitor, atypeFactory);
    }

    public void setInfer(boolean infer) {
        this.infer = infer;
    }

    @Override
    protected void validateWildCardTargetLocation(AnnotatedTypeMirror.AnnotatedWildcardType type, Tree tree) {

        InferenceVisitor<?,?> inferVisitor = (InferenceVisitor<?,?>) visitor;
        if (inferVisitor.ignoreTargetLocations) {
            return;
        }

        AnnotationMirror[] mirrors = new AnnotationMirror[0];
        for (AnnotationMirror am : type.getSuperBound().getAnnotations()) {
            inferVisitor.annoIsNoneOf(type, am,
                    inferVisitor.locationToIllegalQuals.get(TypeUseLocation.LOWER_BOUND).toArray(mirrors),
                    "type.invalid.annotations.on.location", tree);
        }

        for (AnnotationMirror am : type.getExtendsBound().getAnnotations()) {
            inferVisitor.annoIsNoneOf(type, am,
                    inferVisitor.locationToIllegalQuals.get(TypeUseLocation.UPPER_BOUND).toArray(mirrors),
                    "type.invalid.annotations.on.location", tree);
        }
    }
}
