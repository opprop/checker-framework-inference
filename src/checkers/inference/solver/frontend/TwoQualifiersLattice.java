package checkers.inference.solver.frontend;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.javacutil.ErrorReporter;

/**
 * Special Lattice class for two qualifier type system.
 * 
 * @author jianchu
 *
 */
public class TwoQualifiersLattice extends Lattice {

    public TwoQualifiersLattice(Map<AnnotationMirror, Collection<AnnotationMirror>> subType,
            Map<AnnotationMirror, Collection<AnnotationMirror>> superType,
            Map<AnnotationMirror, Collection<AnnotationMirror>> incomparableType,
            Set<? extends AnnotationMirror> allTypes, AnnotationMirror top, AnnotationMirror bottom,
            int numTypes) {
        super(subType, superType, incomparableType, allTypes, top, bottom, numTypes, null, null);
    }

    @Override
    public boolean isSubtype(AnnotationMirror a1, AnnotationMirror a2) {
        if (!allTypes.contains(a1) || !allTypes.contains(a2)) {
            ErrorReporter.errorAbort("Enconture invalid type when perform isSubtype judgement: " +
                    " all type qualifiers in this lattice are: " + allTypes +
                    " but a1 is : + " + a1 + " and a2 is: " + a2);
        }

        if (bottom.equals(a1)) {
            return true;
        } else {
            return top.equals(a2);
        }
    }
}
