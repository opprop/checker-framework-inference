package checkers.inference.solver.backend;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.Serializer;

/**
 * Translator is responsible for encoding/decoding work for Backend.
 *
 * It encode Slot and Constraint to specific types needed by underlying solver,
 * and decode solver solution to AnnotationMirror.
 *
 * @author charleszhuochen
 *
 * @param <S> encoding type for slot.
 * @param <T> encoding type for constraint.
 * @param <A> type for underlying solver's solution of a Slot
 */
public interface FormatTranslator<S, T, A> extends Serializer<S, T> {

    /**
     * Decode solver's solution of a Slot to an AnnotationMirror represent this solution.
     *
     * @param solution solver's solution of a Slot
     * @param processingEnvironment the process environment for creating the AnnotationMirror, if needed
     * @return AnnotationMirror represent this solution
     */
    AnnotationMirror decodeSolution(A solution, ProcessingEnvironment processingEnvironment);
}
