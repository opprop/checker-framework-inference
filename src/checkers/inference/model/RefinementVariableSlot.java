package checkers.inference.model;

/**
 * RefinementVariableSlots represent a potential downward refinement of an existing VariableSlot.
 * For each RefinementVariableSlot, R, there exists a VariableSlot, V, with which it shares the
 * following subtype relationship: R <: V
 *
 * <p>Refinement variables are used to model the semantics of flow-sensitive type refinement
 * (http://types.cs.washington.edu/checker-framework/current/checkers-manual.html#type-refinement).
 *
 * <p>Within methods, the values of fields, parameters, and local variables are all refined downward
 * when possible ( e.g. after an assignment ). To model this behavior, every time a variable could
 * possibly be refined downward we generate a RefinementVariableSlot which is then used in all
 * constraints for which the refinement holds.
 *
 * <p>E.g. The notation @X where X is an integer is used to identify individual VariableSlots below.
 * Each @X corresponds to locations where @VarAnnot(X) would be placed and identifies trees
 * corresponding to the Variable slot with id X.
 *
 * <p>Using the Nullness type-system:
 *
 * <p>class RefVarExample {
 *
 * <p>private @0 String myField = null;
 *
 * <p>void method( ) { @1 String s = myField; myField = "not null"; // This line results in a
 * RefinementVariable @2 s = myField; // This line results in a RefinementVariable @3 } }
 *
 * <p>In the example above the statement: @1 String s = myField; will generate the following subtype
 * constraint: @0 <: @1
 *
 * <p>At this point in method, nothing is known about myField except for the information given by
 * it's declaration. However, after the statement: myField = "not null"; we know explicitly that
 * myField is now not null and data-flow would refine @0 to @NonNull. Any location in which this
 * type of refinement is possible, usually at all method local assignments and potentially at the
 * branches of if statements, we generate refinement variables.
 *
 * <p>Therefore, the assignment myField = "not null" results in a refinement variable @2 with the
 * following constraints: @2 <: @0 @2 <: ConstantSlot( NonNull )
 *
 * <p>Note, that even though @2 and @0 represent annotations on the same declared variable, @2's
 * value might be different than @1 as they represent the possible type of the variable in different
 * locations. For instance, the line: s = myField; generates the following subtype constraints: @2
 * <: @1
 *
 * <p>Essentially, RefinementVariableSlots allow us to generate constraints as if the code were
 * written in SSA form ( http://en.wikipedia.org/wiki/Static_single_assignment_form ).
 */
public class RefinementVariableSlot extends VariableSlot {

    private final Slot refined;

    public RefinementVariableSlot(int id, AnnotationLocation location, Slot refined) {
        super(id, location);
        this.refined = refined;
    }

    public Slot getRefined() {
        return refined;
    }

    @Override
    public <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public Kind getKind() {
        return Kind.REFINEMENT_VARIABLE;
    }

    /**
     * Refinement variables should never be re-inserted into the source code. record does not
     * correspond to an annotatable position.
     *
     * @return false
     */
    @Override
    public boolean isInsertable() {
        return false;
    }
}
