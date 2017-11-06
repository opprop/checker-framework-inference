package checkers.inference.model.tree;

import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class FakeTreeBuilder {

    private final JavacProcessingEnvironment processingEnvironment;

    public FakeTreeBuilder(JavacProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public FakeExtendsBoundTree createFakeExtendsBoundTree(WildcardTree wildcardTree) {
        Name objectName = Names.instance(processingEnvironment.getContext()).java_lang_Object;
        return new FakeExtendsBoundTree(objectName, wildcardTree);
    }
}
