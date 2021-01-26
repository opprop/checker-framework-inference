package refval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;

import refval.qual.RefVal;
import refval.qual.UnknownRefVal;
import refval.util.RefValUtils;

/**
 * RefValAnnotatedTypeFactory is the type factory for RefVal type system. It
 * defines the subtype relationship of RefVal type system, annotate the base
 * cases, and implements simplification algorithm.
 * 
 * @author jianchu
 *
 */
public class RefValAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    protected final AnnotationMirror REFVAL, BOTTOMREFVAL, UnknownREFVAL;
    /**
     * For each Java type is present in the target program, typeNamesMap maps
     * String of the type to the TypeMirror.
     */
    private final Map<String, TypeMirror> typeNamesMap = new HashMap<String, TypeMirror>();

    public RefValAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        REFVAL = AnnotationBuilder.fromClass(elements, RefVal.class);
        BOTTOMREFVAL = RefValUtils.createRefValAnnotation(RefValUtils.convert(""), processingEnv);
        UnknownREFVAL = AnnotationBuilder.fromClass(elements, UnknownRefVal.class);
        postInit();
    }

    @Override
    public TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(super.createTreeAnnotator(), new RefValTreeAnnotator());
    }

    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new RefValQualifierHierarchy(factory, BOTTOMREFVAL);
    }

    /**
     * This method handles autoboxing for primitive type.
     * For statements, Integer i = 3;
     * The annotation for i should be @RefVal(typeNames = {"Integer"}).
     */
    @Override
    public AnnotatedDeclaredType getBoxedType(AnnotatedPrimitiveType type) {
        TypeElement typeElt = types.boxedClass(type.getUnderlyingType());
        AnnotationMirror am = RefValUtils.createRefValAnnotation(typeElt.asType().toString(),
                this.processingEnv);
        AnnotatedDeclaredType dt = fromElement(typeElt);
        dt.addAnnotation(am);
        return dt;
    }

    /**
     * This method handles unboxing for reference type.
     * For statements, int i = new Integer(3);
     * The annotation for i should be @RefVal(typeNames = {"int"}).
     */
    @Override
    public AnnotatedPrimitiveType getUnboxedType(AnnotatedDeclaredType type)
            throws IllegalArgumentException {
        PrimitiveType primitiveType = types.unboxedType(type.getUnderlyingType());
        AnnotationMirror am = RefValUtils.createRefValAnnotation(primitiveType.toString(),
                this.processingEnv);
        AnnotatedPrimitiveType pt = (AnnotatedPrimitiveType) AnnotatedTypeMirror.createType(
                primitiveType, this, false);
        pt.addAnnotation(am);
        return pt;
    }

    private final class RefValQualifierHierarchy extends GraphQualifierHierarchy {

        public RefValQualifierHierarchy(MultiGraphFactory f, AnnotationMirror bottom) {
            super(f, bottom);
        }

        /**
         * This method checks whether rhs is subtype of lhs. rhs and lhs are
         * both RefVal types with typeNameRoots argument.
         * 
         * @param rhs the right hand side annotation mirror
         * @param lhs the left hand side annotation mirror
         * @return true is rhs is subtype of lhs, otherwise return false.
         */
        private boolean isSubtypeWithRoots(AnnotationMirror rhs, AnnotationMirror lhs) {

            Set<String> rTypeNamesSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNames(rhs)));
            Set<String> lTypeNamesSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNames(lhs)));
            Set<String> rRootsSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNameRoots(rhs)));
            Set<String> lRootsSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNameRoots(lhs)));
            Set<String> combinedTypeNames = new HashSet<String>();
            combinedTypeNames.addAll(rTypeNamesSet);
            combinedTypeNames.addAll(lTypeNamesSet);
            Set<String> combinedRoots = new HashSet<String>();
            combinedRoots.addAll(rRootsSet);
            combinedRoots.addAll(lRootsSet);

            AnnotationMirror combinedAnno = RefValUtils.createRefValAnnotationWithRoots(
                    combinedTypeNames, combinedRoots, processingEnv);
            AnnotationMirror refinedCombinedAnno = refineRefVal(combinedAnno);
            AnnotationMirror refinedLhs = refineRefVal(lhs);

            if (AnnotationUtils.areSame(refinedCombinedAnno, refinedLhs)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * This method checks whether rhs is subtype of lhs. rhs and lhs are
         * both RefVal types without typeNameRoots argument. Currently this
         * method is not used, but we can use it for a lightweight RefVal type
         * system. (One without typeNameRoots argument).
         * 
         * @param rhs the right hand side annotation mirror
         * @param lhs the left hand side annotation mirror
         * @return true is rhs is subtype of lhs, otherwise return false.
         */
        private boolean isSubtypeWithoutRoots(AnnotationMirror rhs, AnnotationMirror lhs) {
            Set<String> rTypeNamesSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNames(rhs)));
            Set<String> lTypeNamesSet = new HashSet<String>(Arrays.asList(RefValUtils
                    .getTypeNames(lhs)));
            if (lTypeNamesSet.containsAll(rTypeNamesSet)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            if (AnnotationUtils.areSameByName(rhs, REFVAL)
                    && AnnotationUtils.areSameByName(lhs, REFVAL)) {
                return isSubtypeWithRoots(rhs, lhs);
                // return isSubtypeWithoutRoots(rhs, lhs);
            } else {
                // if (rhs != null && lhs != null)
                if (AnnotationUtils.areSameByName(rhs, REFVAL)) {
                    rhs = REFVAL;
                } else if (AnnotationUtils.areSameByName(lhs, REFVAL)) {
                    lhs = REFVAL;
                }
                return super.isSubtype(rhs, lhs);
            }
        }
    }

    public class RefValTreeAnnotator extends TreeAnnotator {
        public RefValTreeAnnotator() {
            super(RefValAnnotatedTypeFactory.this);
        }

        @Override
        public Void visitNewArray(final NewArrayTree node, final AnnotatedTypeMirror type) {
            AnnotationMirror refValType = RefValUtils.generateRefValAnnoFromNewClass(type,
                    processingEnv);
            TypeMirror tm = type.getUnderlyingType();
            typeNamesMap.put(tm.toString(), tm);
            type.replaceAnnotation(refValType);
            return super.visitNewArray(node, type);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror type) {
            AnnotationMirror refValType = RefValUtils.generateRefValAnnoFromNewClass(type,
                    processingEnv);
            TypeMirror tm = type.getUnderlyingType();
            typeNamesMap.put(tm.toString(), tm);
            type.replaceAnnotation(refValType);
            return super.visitNewClass(node, type);
        }

        @Override
        public Void visitLiteral(LiteralTree node, AnnotatedTypeMirror type) {
            if (!node.getKind().equals(Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror annoType = type;
                AnnotationMirror refValType = RefValUtils.generateRefValAnnoFromLiteral(annoType,
                        processingEnv);
                type.replaceAnnotation(refValType);
            }
            return super.visitLiteral(node, type);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror type) {
            ExecutableElement methodElement = TreeUtils.elementFromUse(node);
            boolean isBytecode = ElementUtils.isElementFromByteCode(methodElement);
            if (isBytecode) {
                AnnotationMirror refValType = RefValUtils.generateRefValAnnoFromByteCode(type,
                        processingEnv);
                TypeMirror tm = type.getUnderlyingType();
                if (tm.getKind() == TypeKind.ARRAY) {
                    replaceArrayComponentATM((AnnotatedArrayType) type);
                }
                typeNamesMap.put(tm.toString(), tm);
                type.replaceAnnotation(refValType);
            }
            return super.visitMethodInvocation(node, type);
        }
    }
    
    /**
     * Simplification algorithm.
     * 
     * @param type the annotation mirror to simplify
     * @return the simplified annotation
     */
    public AnnotationMirror refineRefVal(AnnotationMirror type) {
        String[] typeNameRoots = RefValUtils.getTypeNameRoots(type);
        Set<String> refinedRoots = new HashSet<String>();

        if (typeNameRoots.length == 0) {

        } else if (typeNameRoots.length == 1) {
            refinedRoots.add(typeNameRoots[0]);
        } else {
            List<String> rootsList = new ArrayList<String>(Arrays.asList(typeNameRoots));
            while (rootsList.size() != 0) {
                TypeMirror decType = getTypeMirror(rootsList.get(0));
                if (!isComparable(decType, rootsList)) {
                    refinedRoots.add(rootsList.get(0));
                    rootsList.remove(0);
                }
            }
        }

        String[] typeNames = RefValUtils.getTypeNames(type);
        Arrays.sort(typeNames);
        Set<String> refinedTypeNames = new HashSet<String>();

        if (refinedRoots.size() == 0) {
            refinedTypeNames = new HashSet<String>(Arrays.asList(typeNames));
            return RefValUtils.createRefValAnnotation(refinedTypeNames, processingEnv);
        } else {
            for (String typeName : typeNames) {
                if (typeName == "") {
                    continue;
                }
                TypeMirror decType = getTypeMirror(typeName);
                if (shouldPresent(decType, refinedRoots)) {
                    refinedTypeNames.add(typeName);
                }
            }
        }

        return RefValUtils.createRefValAnnotationWithRoots(refinedTypeNames, refinedRoots,
                processingEnv);
    }

    /**
     * Add the bytecode default RefVal annotation for component type of the given {@link AnnotatedArrayType}.
     *
     *<p> For multi-dimensional array, this method will recursively add bytecode default RefVal annotation to array's component type.
     *
     * @param arrayAtm the given {@link AnnotatedArrayType}, whose component type will be added the bytecode default.
     */
    private void replaceArrayComponentATM(AnnotatedArrayType arrayAtm) {
        AnnotatedTypeMirror componentAtm = arrayAtm.getComponentType();
        AnnotationMirror componentAnno = RefValUtils.generateRefValAnnoFromByteCode(componentAtm,
                processingEnv);
        componentAtm.replaceAnnotation(componentAnno);
        if (componentAtm.getKind() == TypeKind.ARRAY) {
            replaceArrayComponentATM((AnnotatedArrayType) componentAtm);
        }
    }

    private boolean isComparable(TypeMirror decType, List<String> rootsList) {
        for (int i = 1; i < rootsList.size(); i++) {
            if (rootsList.get(i) == "") {
                continue;
            }
            TypeMirror comparedDecType = getTypeMirror(rootsList.get(i));
            if (this.types.isSubtype(comparedDecType, decType)) {
                rootsList.remove(i);
                return true;
            } else if (this.types.isSubtype(decType, comparedDecType)) {
                rootsList.remove(0);
                return true;
            }
        }

        return false;
    }

    private boolean shouldPresent(TypeMirror decType, Set<String> refinedRoots) {
        for (String refinedRoot : refinedRoots) {
            if (refinedRoot == "") {
                continue;
            }
            TypeMirror comparedDecType = getTypeMirror(refinedRoot);
            if (this.types.isSubtype(decType, comparedDecType)) {
                return false;
            } else if (this.types.isSubtype(comparedDecType, decType)) {
                return true;
            }
        }
        return true;
    }

    private TypeMirror getTypeMirror(String typeName) {
        if (this.typeNamesMap.keySet().contains(typeName)) {
            return this.typeNamesMap.get(typeName);
        } else {
            return elements.getTypeElement(convertToReferenceType(typeName)).asType();
        }
    }

    private String convertToReferenceType(String typeName) {
        switch (typeName) {
        case "int":
            return Integer.class.getName();
        case "short":
            return Short.class.getName();
        case "byte":
            return Byte.class.getName();
        case "long":
            return Long.class.getName();
        case "char":
            return Character.class.getName();
        case "float":
            return Float.class.getName();
        case "double":
            return Double.class.getName();
        case "boolean":
            return Boolean.class.getName();
        default:
            return typeName;
        }
    }

    public Map<String, TypeMirror> getTypeNameMap() {
        return this.typeNamesMap;
    }
}
