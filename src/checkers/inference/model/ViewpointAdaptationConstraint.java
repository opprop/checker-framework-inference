package checkers.inference.model;

import java.util.Arrays;

/**
 * Represents a constraint that the viewpoint adaptation between target and decl gives result.
 *
 * TODO: clarify relation to TernaryVariableSlot. Should we add separate slots for VPA?
 */
public class ViewpointAdaptationConstraint extends Constraint implements TernaryConstraint {

    private final Slot target;
    private final Slot decl;
    private final Slot result;

    protected ViewpointAdaptationConstraint(Slot target, Slot decl, Slot result,
            AnnotationLocation location) {
        super(Arrays.asList(target, decl, result), location);
        this.target = target;
        this.decl = decl;
        this.result = result;
    }

    public Slot getTarget() {
        return target;
    }

    public Slot getDeclared() {
        return decl;
    }

    @Override
    public Slot getResult() {
        return result;
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    // First = Target, Second = Declared
    // See ConstraintEncoderCoordinator
    @Override
    public Slot getLeftOperand() {
        return getTarget();
    }

    @Override
    public Slot getRightOperand() {
        return getDeclared();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((decl == null) ? 0 : decl.hashCode());
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ViewpointAdaptationConstraint other = (ViewpointAdaptationConstraint) obj;
        if (decl == null) {
            if (other.decl != null) {
                return false;
            }
        } else if (!decl.equals(other.decl)) {
            return false;
        }
        if (result == null) {
            if (other.result != null) {
                return false;
            }
        } else if (!result.equals(other.result)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }
}
