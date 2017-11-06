package checkers.inference.model.tree;

import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Name;

public class FakeExtendsBoundTree extends JCIdent {

    private final WildcardTree boundedWildcard;

    public FakeExtendsBoundTree(Name name, WildcardTree wildcardTree) {
        super(name, null);
        boundedWildcard = wildcardTree;
    }

    public WildcardTree getBoundedWildcard() {
        return boundedWildcard;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FakeExtendsBoundTree) &&
                (this.boundedWildcard.equals(((FakeExtendsBoundTree) obj).getBoundedWildcard()));
    }

    @Override
    public int hashCode() {
        return boundedWildcard.hashCode();
    }
}
