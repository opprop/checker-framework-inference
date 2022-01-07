package checkers.inference.util;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreeScanner;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeTreesDefaultTypeResolver {

    public static Map<Tree, AnnotatedTypeMirror> resolve(
            CompilationUnitTree root,
            BaseAnnotatedTypeFactory realTypeFactory
    ) {
        TypeTreeVisitor visitor = new TypeTreeVisitor(realTypeFactory);
        visitor.scan(root, null);

        return visitor.defaultTypes;
    }

    private static class TypeTreeVisitor extends TreeScanner<Void, Void> {

        private final BaseAnnotatedTypeFactory realTypeFactory;

        private final Types types;

        private final Map<Tree, AnnotatedTypeMirror> defaultTypes;

        private TypeTreeVisitor(BaseAnnotatedTypeFactory realTypeFactory) {
            this.realTypeFactory = realTypeFactory;
            this.types = realTypeFactory.getProcessingEnv().getTypeUtils();
            this.defaultTypes = new HashMap<>();
        }

        private AnnotatedTypeMirror getDefaultTypeFor(Tree tree) {
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
            AnnotatedTypeMirror.AnnotatedDeclaredType defaultType =
                    (AnnotatedTypeMirror.AnnotatedDeclaredType) getDefaultTypeFor(tree);

            Tree ext = tree.getExtendsClause();
            if (ext != null) {
                for (AnnotatedTypeMirror.AnnotatedDeclaredType superType : defaultType.directSupertypes()) {
                    if (superType.getUnderlyingType().asElement().getKind().isClass()) {
                        defaultTypes.put(ext, superType);
                        break;
                    }
                }
            }

            List<? extends Tree> impls = tree.getImplementsClause();
            if (impls != null) {
                for (Tree im : impls) {
                    for (AnnotatedTypeMirror.AnnotatedDeclaredType superType : defaultType.directSupertypes()) {
                        if (superType.getUnderlyingType().asElement().getKind().isInterface()
                                && types.isSameType(
                                superType.getUnderlyingType(), TreeUtils.typeOf(im))) {
                            defaultTypes.put(im, superType);
                            break;
                        }
                    }
                }
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
//            assert typeArgumentTrees.size() == typeArgumentTypes.size();

            for (int i = 0; i < typeArgumentTrees.size(); ++i) {
                defaultTypes.put(typeArgumentTrees.get(i), typeArgumentTypes.get(i));
            }
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
    }
}
