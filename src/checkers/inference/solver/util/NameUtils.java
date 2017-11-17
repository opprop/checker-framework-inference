package checkers.inference.solver.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class NameUtils {

    public static String getSimpleName(AnnotationMirror annoMirror) {
        final DeclaredType annoType = annoMirror.getAnnotationType();
        final TypeElement elm = (TypeElement) annoType.asElement();
        return elm.getSimpleName().toString().intern();
    }


    /**
     * Remove suffix from the given source string.
     *
     * If the source string doesn't has the given suffix, then return the original source string,
     * without any changes.
     * @param source the source string that contains a suffix
     * @param suffix the suffix to be removed from source
     * @return the string that is trimmed the suffix from source.
     */
    public static String removeSuffix(String source, String suffix) {
        if (source == null || !source.endsWith(suffix)) {
            return source;
        }

       return source.substring(0, source.length() - suffix.length());
    }
}
