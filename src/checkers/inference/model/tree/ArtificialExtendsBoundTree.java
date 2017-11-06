package checkers.inference.model.tree;

import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Name;

public class ArtificialExtendsBoundTree extends JCIdent {

    private final WildcardTree boundedWildcard;

    public ArtificialExtendsBoundTree(Name name, WildcardTree wildcardTree) {
        super(name, null);
        boundedWildcard = wildcardTree;
    }

    public WildcardTree getBoundedWildcard() {
        return boundedWildcard;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ArtificialExtendsBoundTree) &&
                (this.boundedWildcard.equals(((ArtificialExtendsBoundTree) obj).getBoundedWildcard()));
    }

    @Override
    public int hashCode() {
        return boundedWildcard.hashCode();
    }
}
