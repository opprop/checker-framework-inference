package refval.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;

import com.sun.source.tree.LiteralTree;

import refval.qual.RefVal;

/**
 * Utility class for RefVal type system.
 * 
 * @author jianchu
 *
 */
public class RefValUtils {

    public static String[] getTypeNames(AnnotationMirror type) {
        return getReferenceValue(type, "typeNames");
    }

    public static String[] getTypeNameRoots(AnnotationMirror type) {
        return getReferenceValue(type, "typeNameRoots");
    }

    private static String[] getReferenceValue(AnnotationMirror type, String valueName) {
        List<String> allTypesList = AnnotationUtils.getElementValueArray(type, valueName, String.class,
                true);
        // types in this list is org.checkerframework.framework.util.AnnotationBuilder.
        String[] allTypesInArray = new String[allTypesList.size()];
        int i = 0;
        for (Object o : allTypesList) {
            allTypesInArray[i] = o.toString();
            i++;
        }
        return allTypesInArray;
    }

    public static AnnotationMirror createRefValAnnotationForByte(String[] refValType,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RefVal.class);
        builder.setValue("typeNameRoots", refValType);
        return builder.build();
    }

    private static AnnotationMirror createRefValAnnotation(final Set<String> refValTypes,
            final AnnotationBuilder builder) {
        String[] refValTypesInArray = new String[refValTypes.size()];
        int i = 0;
        for (String refValType : refValTypes) {
            refValTypesInArray[i] = refValType.toString();
            i++;
        }
        builder.setValue("typeNames", refValTypesInArray);
        return builder.build();
    }

    private static AnnotationMirror createRefValAnnotationWithoutName(final Set<String> roots,
            final AnnotationBuilder builder) {
        String[] refValTypesInArray = new String[roots.size()];
        int i = 0;
        for (String refValType : roots) {
            refValTypesInArray[i] = refValType.toString();
            i++;
        }
        builder.setValue("typeNameRoots", refValTypesInArray);
        return builder.build();
    }

    public static AnnotationMirror createRefValAnnotation(Set<String> refValTypes,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RefVal.class);

        return createRefValAnnotation(refValTypes, builder);
    }

    public static AnnotationMirror createRefValAnnotationWithoutName(Set<String> roots,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RefVal.class);
        return createRefValAnnotationWithoutName(roots, builder);

    }

    public static AnnotationMirror createRefValAnnotation(String[] refValType,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RefVal.class);
        builder.setValue("typeNames", refValType);
        return builder.build();
    }

    public static AnnotationMirror createRefValAnnotationWithRoots(Set<String> refValTypes,
            Set<String> refValTypesRoots, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RefVal.class);
        return createRefValAnnotationWithRoots(refValTypes, refValTypesRoots, builder);
    }

    private static AnnotationMirror createRefValAnnotationWithRoots(final Set<String> refValTypes,
            final Set<String> refValTypesRoots, final AnnotationBuilder builder) {
        String[] refValTypesInArray = new String[refValTypes.size()];
        int i = 0;
        for (String refValType : refValTypes) {
            refValTypesInArray[i] = refValType.toString();
            i++;
        }

        String[] refValTypesRootInArray = new String[refValTypesRoots.size()];
        int j = 0;
        for (String refValTypesRoot : refValTypesRoots) {
            refValTypesRootInArray[j] = refValTypesRoot.toString();
            j++;
        }
        if (refValTypesRootInArray.length > 0) {
            builder.setValue("typeNameRoots", refValTypesRootInArray);
        }
        if (refValTypesInArray.length > 0) {
            builder.setValue("typeNames", refValTypesInArray);
        }

        return builder.build();
    }

    public static AnnotationMirror generateRefValAnnoFromNewClass(AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror refValType = createRefValAnnotation(convert(className), processingEnv);
        return refValType;
    }

    public static AnnotationMirror generateRefValAnnoFromByteCode(AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror refValType = createRefValAnnotationForByte(convert(className),
                processingEnv);
        return refValType;
    }

    public static AnnotationMirror generateRefValAnnoFromLiteral(AnnotatedTypeMirror type,
            ProcessingEnvironment processingEnv) {
        String refValTypeInArray[] = convert(type.getUnderlyingType().toString());
        AnnotationMirror refValType = createRefValAnnotation(refValTypeInArray, processingEnv);
        return refValType;
    }

    public static AnnotationMirror generateRefValAnnoFromLiteral(LiteralTree node,
            ProcessingEnvironment processingEnv) {
        String refValTypeInArray[] = { "" };
        switch (node.getKind()) {
        case STRING_LITERAL:
            refValTypeInArray = convert(String.class.toString().split(" ")[1]);
            break;
        case INT_LITERAL:
            refValTypeInArray = convert(int.class.toString());
            break;
        case LONG_LITERAL:
            refValTypeInArray = convert(long.class.toString());
            break;
        case FLOAT_LITERAL:
            refValTypeInArray = convert(float.class.toString());
            break;
        case DOUBLE_LITERAL:
            refValTypeInArray = convert(double.class.toString());
            break;
        case BOOLEAN_LITERAL:
            refValTypeInArray = convert(boolean.class.toString());
            break;
        case CHAR_LITERAL:
            refValTypeInArray = convert(char.class.toString());
            break;
        case NULL_LITERAL:
            // Null literal wouldn't be passed here.
            break;
        default:
            throw new BugInCF("Unknown literal tree: " + node.getKind().toString());
        }
        AnnotationMirror refValType = createRefValAnnotation(refValTypeInArray, processingEnv);
        return refValType;
    }

    public static String[] convert(String... typeName) {
        return typeName;
    }

    public static AnnotationMirror createRefValAnnotation(String typeName,
            ProcessingEnvironment processingEnv) {
        Set<String> typeNames = new HashSet<String>();
        typeNames.add(typeName);
        AnnotationMirror am = RefValUtils.createRefValAnnotation(typeNames, processingEnv);
        return am;
    }
}
