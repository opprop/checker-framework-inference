package checkers.inference.model;

import java.util.Arrays;

import org.checkerframework.dataflow.util.HashCodeUtils;
import org.checkerframework.javacutil.BugInCF;

/**
 * Represents a preference for a particular qualifier.
 */
public class PreferenceConstraint extends Constraint {

    private final VariableSlot variable;
    private final ConstantSlot goal;
    private final int weight;

    private PreferenceConstraint(VariableSlot variable, ConstantSlot goal, int weight,
            AnnotationLocation location) {
        super(Arrays.<Slot> asList(variable, goal), location);
        this.variable = variable;
        this.goal = goal;
        this.weight = weight;
    }

    protected static PreferenceConstraint create(VariableSlot variable, ConstantSlot goal,
            int weight, AnnotationLocation location) {
        if (variable == null || goal == null) {
            throw new BugInCF("Create preference constraint with null argument. Variable: "
                    + variable + " Goal: " + goal);
        }

        return new PreferenceConstraint(variable, goal, weight, location);
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    public VariableSlot getVariable() {
        return variable;
    }

    public ConstantSlot getGoal() {
        return goal;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return HashCodeUtils.hash(6151, variable, goal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PreferenceConstraint other = (PreferenceConstraint) obj;
        return variable.equals(other.variable) && goal.equals(other.goal);
    }
}
