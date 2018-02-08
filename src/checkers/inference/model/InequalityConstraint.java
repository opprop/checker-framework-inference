package checkers.inference.model;

import java.util.Arrays;

public class InequalityConstraint extends Constraint implements BinaryConstraint {

    private final Slot lhs;
    private final Slot rhs;

    protected InequalityConstraint(Slot lhs, Slot rhs) {
        super(Arrays.asList(lhs, rhs));
        this.lhs = lhs;
        this.rhs = rhs;
    }

    protected InequalityConstraint(Slot lhs, Slot rhs, AnnotationLocation location) {
        super(Arrays.asList(lhs, rhs), location);
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
        return new InequalityConstraint(lhs, rhs);
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
        InequalityConstraint other = (InequalityConstraint) obj;
        if ((lhs.equals(other.lhs) && rhs.equals(other.rhs))
                || (lhs.equals(other.rhs) && (rhs.equals(other.lhs)))) {
            return true;
        } else {
            return false;
        }
    }
}
