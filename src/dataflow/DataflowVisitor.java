package dataflow;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

import javax.lang.model.element.ExecutableElement;

/**
 * Don't generate any special constraint for Dataflow type system.
 * 
 * @author jianchu
 *
 */
public class DataflowVisitor extends InferenceVisitor<DataflowChecker, BaseAnnotatedTypeFactory> {

    public DataflowVisitor(DataflowChecker checker, InferenceChecker ichecker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);
    }

    /**
     * Skip this test because every class has at least one non-top upper bound
     * determined by the Java type it extends.
     */
    @Override
    protected void checkConstructorResult(
            AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {
    }
}
