package checkers.inference.model.tree;

import org.checkerframework.dataflow.util.HashCodeUtils;

import com.sun.source.tree.WildcardTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;

public class ArtificialExtendsBoundTree extends JCIdent {

    private final WildcardTree boundedWildcard;

    public ArtificialExtendsBoundTree(WildcardTree wildcardTree) {
        super(null, null);
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
        return HashCodeUtils.hash("artificial", boundedWildcard);
    }
}
