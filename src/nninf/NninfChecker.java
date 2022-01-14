package nninf;

import nninf.qual.KeyFor;
import nninf.qual.UnknownKeyFor;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.dataflow.InferenceAnalysis;
import nninf.qual.NonNull;
import nninf.qual.Nullable;

public class NninfChecker extends BaseInferrableChecker {
    public AnnotationMirror NULLABLE, NONNULL, UNKNOWNKEYFOR, KEYFOR;

    @Override
    public void initChecker() {
        final Elements elements = processingEnv.getElementUtils();
        NULLABLE = AnnotationBuilder.fromClass(elements, Nullable.class);
        NONNULL  = AnnotationBuilder.fromClass(elements, NonNull.class);
        UNKNOWNKEYFOR = AnnotationBuilder.fromClass(elements, UnknownKeyFor.class);
        KEYFOR = AnnotationBuilder.fromClass(
                elements,
                KeyFor.class,
                AnnotationBuilder.elementNamesValues("value", new String[0])
        );

        super.initChecker();
    }

    @Override
    public NninfVisitor createVisitor(InferenceChecker ichecker, BaseAnnotatedTypeFactory factory, boolean infer)  {
        return new NninfVisitor(this, ichecker, factory, infer);
    }

    @Override
    public NninfAnnotatedTypeFactory createRealTypeFactory(boolean infer) {
        return new NninfAnnotatedTypeFactory(this, infer);
    }

    @Override
    public CFTransfer createInferenceTransferFunction(InferenceAnalysis analysis) {
        return new NninfTransfer(analysis);
    }
}