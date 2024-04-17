package sparta.checkers;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;

import org.checkerframework.framework.qual.StubFiles;

import checkers.inference.BaseInferrableChecker;

/**
 * Checker for inferring @Sink annotations for SPARTA.
 *
 * <p>Only standard subtyping rules are needed so no methods are overridden.
 */
@StubFiles("information_flow.astub")
public class IFlowSinkChecker extends BaseInferrableChecker {

    @Override
    public boolean isConstant(Tree node) {
        return (node instanceof LiteralTree);
    }

    @Override
    public SimpleFlowAnnotatedTypeFactory createRealTypeFactory(boolean infer) {
        return new SimpleFlowAnnotatedTypeFactory(this, infer);
    }

    @Override
    public boolean shouldStoreConstantSlots() {
        return false;
    }
}
