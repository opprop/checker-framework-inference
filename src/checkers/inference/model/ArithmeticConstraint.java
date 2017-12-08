package checkers.inference.model;

import java.util.Arrays;
import org.checkerframework.javacutil.ErrorReporter;
import com.sun.source.tree.Tree.Kind;

/**
 * Represents a constraint between the result of an arithmetic operation between two operands.
 * Subclasses of this constraint class denote each specific kind of arithmetic operation, such as
 * add, sub, mul, div, and mod.
 *
 * <p>
 * This abstract class defines the fields and accessors shared by all arithmetic operations.
 */
public abstract class ArithmeticConstraint extends Constraint implements TernaryConstraint {

    private final Slot lefthandside;
    private final Slot righthandside;
    private final Slot result;

    protected ArithmeticConstraint(Slot lhs, Slot rhs, Slot result, AnnotationLocation location) {
        super(Arrays.asList(lhs, rhs, result), location);
        this.lefthandside = lhs;
        this.righthandside = rhs;
        this.result = result;
    }

    // Alternative getter names
    public Slot getLHS() {
        return lefthandside;
    }

    public Slot getRHS() {
        return righthandside;
    }

    public Slot getResult() {
        return result;
    }

    @Override
    public Slot getFirst() {
        return getLHS();
    }

    @Override
    public Slot getSecond() {
        return getRHS();
    }

    @Override
    public Slot getThird() {
        return getResult();
    }

    @Override
    public int hashCode() {
        int hc = 1;
        hc += ((lefthandside == null) ? 0 : lefthandside.hashCode());
        hc += ((righthandside == null) ? 0 : righthandside.hashCode());
        hc += ((result == null) ? 0 : result.hashCode());
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArithmeticConstraint other = (ArithmeticConstraint) obj;
        if (lefthandside.equals(other.lefthandside) && righthandside.equals(other.righthandside)
                && result.equals(other.result)) {
            return true;
        } else {
            return false;
        }
    }
}
