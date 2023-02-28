package checkers.inference.util;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreeScanner;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to help SlotManager to find the real default type of
 * each slot.
 *
 * Slot manager tries to create a slot for each type declaration or type
 * use in the class, so the associated tree is usually a type tree.
 * Currently, AnnotatedTypeFactory cannot properly determine the annotated
 * type of the type tree because it's unaware of the tree's location.
 *
 * For example, AnnotatedTypeFactory returns the same annotated type for
 * "Object" in the following two cases:
 * 1. Object s = "123";
 * 2. List<? extends Object> l = new ArrayList<>();
 * But it's possible to have a different default type for the upperbound.
 *
 * This class aims to properly find the real default type for type trees.
 */
public class SlotDefaultTypeResolver {

    public static Map<Tree, AnnotatedTypeMirror> resolve(
            ClassTree classTree,
            BaseAnnotatedTypeFactory realTypeFactory
    ) {
        DefaultTypeFinder finder = new DefaultTypeFinder(realTypeFactory);
        finder.scan(classTree, null);

        return finder.defaultTypes;
    }

    /**
     * A tree visitor that focuses on collecting the real default types for
     * type trees under this tree.
     *
     * The results may contain information for trees that are not a type
     * tree. For example, the implements clause of a class can either be
     * an IdentifierTree or a ParameterizedTypeTree, but we always store
     * its type for simplicity.
     */
    private static class DefaultTypeFinder extends TreeScanner<Void, Void> {

        private final BaseAnnotatedTypeFactory realTypeFactory;

        // A mapping from a tree to its real default type.
        private final Map<Tree, AnnotatedTypeMirror> defaultTypes;

        private DefaultTypeFinder(BaseAnnotatedTypeFactory realTypeFactory) {
            this.realTypeFactory = realTypeFactory;
            this.defaultTypes = new HashMap<>();
        }

        // Each visit method should call this method to get the default
        // type of its argument. This ensures the correct type information
        // is propagated downwards, especially for nested type trees.
        private AnnotatedTypeMirror getDefaultTypeFor(Tree tree) {
            // use a cached type when possible
            AnnotatedTypeMirror defaultType = defaultTypes.get(tree);
            if (defaultType == null) {
                if (TreeUtils.isTypeTree(tree)) {
                    defaultType = realTypeFactory.getAnnotatedTypeFromTypeTree(tree);
                } else {
                    defaultType = realTypeFactory.getAnnotatedType(tree);
                }

                defaultTypes.put(tree, defaultType);
            }

            return defaultType;
        }

        @Override
        public Void visitClass(ClassTree tree, Void unused) {
            // stores the default type of the class tree
            getDefaultTypeFor(tree);

            Tree ext = tree.getExtendsClause();
            if (ext != null) {
                defaultTypes.put(ext, realTypeFactory.getTypeOfExtendsImplements(ext));
            }

            List<? extends Tree> impls = tree.getImplementsClause();
            for (Tree im : impls) {
                defaultTypes.put(im, realTypeFactory.getTypeOfExtendsImplements(im));
            }

            return super.visitClass(tree, unused);
        }

        @Override
        public Void visitPrimitiveType(PrimitiveTypeTree tree, Void unused) {
            getDefaultTypeFor(tree);
            return super.visitPrimitiveType(tree, unused);
        }

        @Override
        public Void visitArrayType(ArrayTypeTree tree, Void unused) {
            AnnotatedTypeMirror.AnnotatedArrayType defaultType =
                    (AnnotatedTypeMirror.AnnotatedArrayType) getDefaultTypeFor(tree);

            defaultTypes.put(tree.getType(), defaultType.getComponentType());

            return super.visitArrayType(tree, unused);
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, Void unused) {
            AnnotatedTypeMirror.AnnotatedDeclaredType defaultType =
                    (AnnotatedTypeMirror.AnnotatedDeclaredType) getDefaultTypeFor(tree);

            List<? extends Tree> typeArgumentTrees = tree.getTypeArguments();
            List<AnnotatedTypeMirror> typeArgumentTypes = defaultType.getTypeArguments();

            /*
            Note that it's not guaranteed that typeArgumentTrees.size() == typeArgumentTypes.size().
            For example:
            List<String> strs = new ArrayList<>()

            In the tree of `ArrayList<>`, we have no type arguments, but its type does have "String"
            as its type argument.
             */
            assert typeArgumentTrees.size() == 0 || typeArgumentTrees.size() == typeArgumentTypes.size();
            for (int i = 0; i < typeArgumentTrees.size(); ++i) {
                defaultTypes.put(typeArgumentTrees.get(i), typeArgumentTypes.get(i));
            }

            // Sometimes, the slot manager annotates the underlying type tree instead of this ParameterizedTypeTree
            defaultTypes.put(tree.getType(), defaultType.getErased());

            return super.visitParameterizedType(tree, unused);
        }

        @Override
        public Void visitWildcard(WildcardTree tree, Void unused) {
            AnnotatedTypeMirror.AnnotatedWildcardType defaultType =
                    (AnnotatedTypeMirror.AnnotatedWildcardType) getDefaultTypeFor(tree);

            if (tree.getKind() == Tree.Kind.EXTENDS_WILDCARD) {
                defaultTypes.put(tree.getBound(), defaultType.getExtendsBound());
            } else if (tree.getKind() == Tree.Kind.SUPER_WILDCARD) {
                defaultTypes.put(tree.getBound(), defaultType.getSuperBound());
            }

            return super.visitWildcard(tree, unused);
        }

        @Override
        public Void visitTypeParameter(TypeParameterTree tree, Void unused) {
            AnnotatedTypeMirror.AnnotatedTypeVariable defaultType =
                    (AnnotatedTypeMirror.AnnotatedTypeVariable) getDefaultTypeFor(tree);

            // Annotated types for TypeParameterTree have the following format:
            // @LowerBound T extends @UpperBound ...
            // So the type of this tree should really mean the lower bound.
            defaultTypes.put(tree, defaultType.getLowerBound());
            for (Tree bound : tree.getBounds()) {
                defaultTypes.put(bound, defaultType.getUpperBound());
            }

            return super.visitTypeParameter(tree, unused);
        }

        @Override
        public Void visitAnnotatedType(AnnotatedTypeTree tree, Void unused) {
            AnnotatedTypeMirror defaultType = getDefaultTypeFor(tree);

            defaultTypes.put(tree.getUnderlyingType(), defaultType);

            return super.visitAnnotatedType(tree, unused);
        }

        @Override
        public Void visitNewClass(NewClassTree tree, Void unused) {
            AnnotatedTypeMirror defaultType = getDefaultTypeFor(tree);
            defaultTypes.put(tree.getIdentifier(), defaultType);
            return super.visitNewClass(tree, unused);
        }
    }
}
