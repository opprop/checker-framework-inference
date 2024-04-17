package dataflow.util;

import com.sun.source.tree.LiteralTree;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TreeUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import dataflow.qual.DataFlow;

/**
 * Utility class for Dataflow type system.
 *
 * @author jianchu
 */
public class DataflowUtils {

    /** The DataFlow.typeNames element/field. */
    private final ExecutableElement dataflowTypeNamesElement;

    /** The DataFlow.typeNameRoots element/field. */
    private final ExecutableElement dataflowTypeNameRootsElement;

    public DataflowUtils(ProcessingEnvironment processingEnv) {
        dataflowTypeNamesElement = TreeUtils.getMethod(DataFlow.class, "typeNames", processingEnv);
        dataflowTypeNameRootsElement =
                TreeUtils.getMethod(DataFlow.class, "typeNameRoots", processingEnv);
    }

    public List<String> getTypeNames(AnnotationMirror type) {
        return AnnotationUtils.getElementValueArray(
                type, dataflowTypeNamesElement, String.class, Collections.emptyList());
    }

    public List<String> getTypeNameRoots(AnnotationMirror type) {
        return AnnotationUtils.getElementValueArray(
                type, dataflowTypeNameRootsElement, String.class, Collections.emptyList());
    }

    public static AnnotationMirror createDataflowAnnotationForByte(
            String[] dataType, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNameRoots", dataType);
        return builder.build();
    }

    private static AnnotationMirror createDataflowAnnotation(
            final Set<String> datatypes, final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNames", datatypesInArray);
        return builder.build();
    }

    private static AnnotationMirror createDataflowAnnotationWithoutName(
            final Set<String> roots, final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[roots.size()];
        int i = 0;
        for (String datatype : roots) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }
        builder.setValue("typeNameRoots", datatypesInArray);
        return builder.build();
    }

    public static AnnotationMirror createDataflowAnnotation(
            Set<String> datatypes, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);

        return createDataflowAnnotation(datatypes, builder);
    }

    public static AnnotationMirror createDataflowAnnotationWithoutName(
            Set<String> roots, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        return createDataflowAnnotationWithoutName(roots, builder);
    }

    public static AnnotationMirror createDataflowAnnotation(
            String[] dataType, ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        builder.setValue("typeNames", dataType);
        return builder.build();
    }

    public static AnnotationMirror createDataflowAnnotationWithRoots(
            Set<String> datatypes,
            Set<String> datatypesRoots,
            ProcessingEnvironment processingEnv) {
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, DataFlow.class);
        return createDataflowAnnotationWithRoots(datatypes, datatypesRoots, builder);
    }

    private static AnnotationMirror createDataflowAnnotationWithRoots(
            final Set<String> datatypes,
            final Set<String> datatypesRoots,
            final AnnotationBuilder builder) {
        String[] datatypesInArray = new String[datatypes.size()];
        int i = 0;
        for (String datatype : datatypes) {
            datatypesInArray[i] = datatype.toString();
            i++;
        }

        String[] datatypesRootInArray = new String[datatypesRoots.size()];
        int j = 0;
        for (String datatypesRoot : datatypesRoots) {
            datatypesRootInArray[j] = datatypesRoot.toString();
            j++;
        }
        if (datatypesRootInArray.length > 0) {
            builder.setValue("typeNameRoots", datatypesRootInArray);
        }
        if (datatypesInArray.length > 0) {
            builder.setValue("typeNames", datatypesInArray);
        }

        return builder.build();
    }

    public static AnnotationMirror genereateDataflowAnnoFromNewClass(
            AnnotatedTypeMirror type, ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror dataFlowType = createDataflowAnnotation(convert(className), processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror genereateDataflowAnnoFromByteCode(
            AnnotatedTypeMirror type, ProcessingEnvironment processingEnv) {
        TypeMirror tm = type.getUnderlyingType();
        String className = tm.toString();
        AnnotationMirror dataFlowType =
                createDataflowAnnotationForByte(convert(className), processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror generateDataflowAnnoFromLiteral(
            AnnotatedTypeMirror type, ProcessingEnvironment processingEnv) {
        String datatypeInArray[] = convert(type.getUnderlyingType().toString());
        AnnotationMirror dataFlowType = createDataflowAnnotation(datatypeInArray, processingEnv);
        return dataFlowType;
    }

    public static AnnotationMirror generateDataflowAnnoFromLiteral(
            LiteralTree node, ProcessingEnvironment processingEnv) {
        String datatypeInArray[] = {""};
        switch (node.getKind()) {
            case STRING_LITERAL:
                datatypeInArray = convert(String.class.toString().split(" ")[1]);
                break;
            case INT_LITERAL:
                datatypeInArray = convert(int.class.toString());
                break;
            case LONG_LITERAL:
                datatypeInArray = convert(long.class.toString());
                break;
            case FLOAT_LITERAL:
                datatypeInArray = convert(float.class.toString());
                break;
            case DOUBLE_LITERAL:
                datatypeInArray = convert(double.class.toString());
                break;
            case BOOLEAN_LITERAL:
                datatypeInArray = convert(boolean.class.toString());
                break;
            case CHAR_LITERAL:
                datatypeInArray = convert(char.class.toString());
                break;
            case NULL_LITERAL:
                // Null literal wouldn't be passed here.
                break;
            default:
                throw new BugInCF("Unknown literal tree: " + node.getKind().toString());
        }
        AnnotationMirror dataFlowType = createDataflowAnnotation(datatypeInArray, processingEnv);
        return dataFlowType;
    }

    public static String[] convert(String... typeName) {
        return typeName;
    }

    public static AnnotationMirror createDataflowAnnotation(
            String typeName, ProcessingEnvironment processingEnv) {
        Set<String> typeNames = new HashSet<String>();
        typeNames.add(typeName);
        AnnotationMirror am = DataflowUtils.createDataflowAnnotation(typeNames, processingEnv);
        return am;
    }
}
