package checkers.inference;

import java.util.Properties;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import javax.lang.model.element.TypeElement;

public class InferenceChecker extends BaseTypeChecker {

    /**
     * This field stores current top level class tree that's being type processed.
     *
     * Note that trees that are not within this tree may be missing some information
     * (in the JCTree implementation), and this is because they are either not fully
     * initialized or being garbage-recycled.
     */
    private ClassTree currentTopLevelClass;

    @Override
    public void initChecker() {
        InferenceMain.getInstance().recordInferenceCheckerInstance(this);
        // Needed for error messages and reporting.
        super.initChecker();
        // Overrides visitor created by initChecker
        this.visitor = InferenceMain.getInstance().getVisitor();
        this.currentTopLevelClass = null;
    }

    /**
     * Called during super.initChecker(). We want it to do nothing.
     */
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return null;
    }

    @Override
    public void typeProcess(TypeElement element, TreePath treePath) {
        // As the entry point for type processing, each time we will receive
        // one fully-analyzed class directly under a compilation unit tree.
        // Please check the documentation in AbstractTypeProcessor for more details.
        this.currentTopLevelClass = (ClassTree) treePath.getLeaf();
        super.typeProcess(element, treePath);
    }

    public ClassTree getCurrentTopLevelClass() {
        return currentTopLevelClass;
    }
}
