package checkers.inference.model;

import java.util.Arrays;

/**
 * Represents an equality relationship between two slots.
 * E.g.
 *  List<String> ls = new ArrayList<String>();
 *
 * If, in any type system:
 *    // vls represents the variable corresponding to the annotation on the first type String
 *    // located on the left-hand side of the assignment in List<String> ls ...
 *    vs = VariableSlot( astPathToVls, 0 )
 *
 *    // als represents the variable corresponding to the annotation on the second type String
 *    // located on the right-hand side of the assignment in ArrayList<String>()
 *    va = VariableSlot( astPathToAls, 1 )
 *
 * Then:
 *   The above statements would result in the following EqualityConstraints:
 *   logical representation:           in Java:
 *   vls == als                        new EqualityConstraint( vls, als )
 *
 * Note: The equality relationship is commutative so order should not matter, though in practice
 * it is up to the given ConstraintSolver to treat them this way.
 */
public class EqualityConstraint extends Constraint implements BinaryConstraint {

    private final Slot lhs;
    private final Slot rhs;

    protected EqualityConstraint(Slot lhs, Slot rhs) {
        super(Arrays.asList(lhs, rhs));
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected EqualityConstraint(Slot lhs, Slot rhs, AnnotationLocation location) {
        super(Arrays.asList(lhs, rhs));
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public Slot getLHS() {
        return lhs;
    }

    @Override
    public Slot getRHS() {
        return rhs;
    }

    @Override
    public Constraint make(Slot lhs, Slot rhs) {
        return new EqualityConstraint(lhs, rhs);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result + ((lhs == null) ? 0 : lhs.hashCode());
        result = result + ((rhs == null) ? 0 : rhs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EqualityConstraint other = (EqualityConstraint) obj;
        if ((lhs.equals(other.lhs) && rhs.equals(other.rhs))
                || (lhs.equals(other.rhs) && (rhs.equals(other.lhs)))) {
            return true;
        } else {
            return false;
        }
    }
}
