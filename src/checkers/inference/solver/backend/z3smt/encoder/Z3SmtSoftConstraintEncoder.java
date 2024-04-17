package checkers.inference.solver.backend.z3smt.encoder;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import org.checkerframework.javacutil.BugInCF;

import java.util.Collection;

import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

public abstract class Z3SmtSoftConstraintEncoder<SlotEncodingT, SlotSolutionT>
        extends Z3SmtAbstractConstraintEncoder<SlotEncodingT, SlotSolutionT> {

    protected final StringBuilder softConstraints;

    public Z3SmtSoftConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator) {
        super(lattice, ctx, z3SmtFormatTranslator);
        this.softConstraints = new StringBuilder();
    }

    protected void addSoftConstraint(Expr serializedConstraint, int weight) {
        softConstraints.append(
                "(assert-soft " + serializedConstraint + " :weight " + weight + ")\n");
    }

    protected abstract void encodeSoftSubtypeConstraint(SubtypeConstraint constraint);

    protected abstract void encodeSoftEqualityConstraint(EqualityConstraint constraint);

    protected abstract void encodeSoftInequalityConstraint(InequalityConstraint constraint);

    /**
     * Encode a set of constraints as soft constraints.
     *
     * @param constraints constraints to be encoded as soft constraints
     * @return a string representation of the encoding of soft constraints
     */
    public String encodeAndGetSoftConstraints(Collection<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (constraint instanceof SubtypeConstraint) {
                encodeSoftSubtypeConstraint((SubtypeConstraint) constraint);

            } else if (constraint instanceof EqualityConstraint) {
                encodeSoftEqualityConstraint((EqualityConstraint) constraint);

            } else if (constraint instanceof InequalityConstraint) {
                encodeSoftInequalityConstraint((InequalityConstraint) constraint);

            } else {
                throw new BugInCF(
                        "Soft constraint for "
                                + constraint.getClass().getName()
                                + " is not supported");
            }
        }
        String res = softConstraints.toString();
        // clear field for next usage
        softConstraints.setLength(0);
        return res;
    }
}
