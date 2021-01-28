package checkers.inference.model;

/**
 * ComparisonVariableSlot represent the left side refinement of an comparison operation between two other
 * {@link VariableSlot}s. 
 * e.g., for a comparison constraint c := x &lt y, the comparison variable slot c is the refined value of
 * x where x < y is always when x = c.
 * Note that this slot is serialized identically to a {@link VariableSlot}.
 */
public class ComparisonVariableSlot extends Slot {

	private final Slot refined;
	
    public ComparisonVariableSlot(AnnotationLocation location, int id, Slot refined) {
        super(id, location);
        this.refined = refined;
    }

    public Slot getRefined() {
        return refined;
    }

    @Override
    public Kind getKind() {
        return Kind.COMPARISON_VARIABLE;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    /**
     * ComparisonVariable should never be re-inserted into the source code.
     *
     * @return false
     */
    @Override
    public boolean isInsertable() {
        return false;
    }
}
