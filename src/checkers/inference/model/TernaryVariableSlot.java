package checkers.inference.model;

/**
 * TernaryVariableSlots represent locations whose values depend on two other VariableSlots.
 *
 * TernaryVariableSlots are used as the result slot for constraints such as viewpoint adaptation,
 * least-upper-bounds, arithmetic operations, and joins between variable and refinement variables.
 * 
 * TODO: decouple the multi-use of TernaryVariableSlots into separate, distinctly named slots for
 * each purpose, ie ViewpointAdaptationResultSlot, LeastUpperBoundSlot, ...
 */
public class TernaryVariableSlot extends VariableSlot {

    private final Slot leftOperand;
    private final Slot rightOperand;

    public TernaryVariableSlot(AnnotationLocation location, int id, Slot leftOperand,
            Slot rightOperand) {
        super(location, id);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    public Kind getKind() {
        return Kind.TERNARY_VARIABLE;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    public Slot getLeftOperand() {
        return leftOperand;
    }

    public Slot getRightOperand() {
        return rightOperand;
    }

    /**
     * TernaryVariableSlots should never be re-inserted into the source code. Record does not
     * correspond to an annotatable position.
     *
     * @return false
     */
    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TernaryVariableSlot other = (TernaryVariableSlot) obj;
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
        if (rightOperand == null) {
            if (other.rightOperand != null) {
                return false;
            }
        } else if (!rightOperand.equals(other.rightOperand)) {
            return false;
        }
        return true;
    }
}
