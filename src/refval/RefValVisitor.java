package refval;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;

import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;

/**
 * Don't generate any special constraint for RefVal type system.
 * 
 * @author jianchu
 *
 */
public class RefValVisitor extends InferenceVisitor<RefValChecker, BaseAnnotatedTypeFactory> {

    public RefValVisitor(RefValChecker checker, InferenceChecker ichecker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        super(checker, ichecker, factory, infer);
    }
}
