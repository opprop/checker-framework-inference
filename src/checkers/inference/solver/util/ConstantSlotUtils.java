package checkers.inference.solver.util;

import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.frontend.Lattice;

/**
 * Utils methods for constant slot.
 * 
 * @author jianchu
 *
 */
public class ConstantSlotUtils {

    //FIXME: passing c1 and c2 is ugly. This made an assumption on the order of c1 and c2
    // should be consistant with the order in the given constraint, which is totally depends on
    // developers without a runtime check. Should refactor this.
    public static boolean checkConstant(ConstantSlot constant1, ConstantSlot constant2,
            Constraint constraint, Lattice lattice) {

        AnnotationMirror annoMirror1 = constant1.getValue();
        AnnotationMirror annoMirror2 = constant2.getValue();

        if (constraint instanceof SubtypeConstraint) {
            if (!lattice.isSubtype(annoMirror1, annoMirror2)) {
                return false;
            }
        } else if (constraint instanceof EqualityConstraint) {
            if (!AnnotationUtils.areSame(annoMirror1, annoMirror2)) {
                return false;
            }
        } else if (constraint instanceof InequalityConstraint) {
            if (AnnotationUtils.areSame(annoMirror1, annoMirror2)) {
                return false;
            }
        } else if (constraint instanceof ComparableConstraint) {
            if (!lattice.isSubtype(annoMirror1, annoMirror2) && !lattice.isSubtype(annoMirror2, annoMirror1)) {
                return false;
            }
        }
        return true;
    }
}
