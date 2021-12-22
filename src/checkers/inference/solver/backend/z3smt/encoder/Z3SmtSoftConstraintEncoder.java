package checkers.inference.solver.backend.z3smt.encoder;

import java.util.Collection;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import checkers.inference.model.ArithmeticConstraint;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ImplicationConstraint;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.PreferenceConstraint;
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
        softConstraints.append("(assert-soft " + serializedConstraint + " :weight " + weight + ")\n");
    }


    protected abstract void encodeSoftSubtypeConstraint(SubtypeConstraint constraint);

    protected abstract void encodeSoftComparableConstraint(ComparableConstraint constraint);

    protected abstract void encodeSoftArithmeticConstraint(ArithmeticConstraint constraint);

    protected abstract void encodeSoftEqualityConstraint(EqualityConstraint constraint);

    protected abstract void encodeSoftInequalityConstraint(InequalityConstraint constraint);

    protected abstract void encodeSoftImplicationConstraint(ImplicationConstraint constraint);

    protected abstract void encodeSoftExistentialConstraint(ExistentialConstraint constraint);

    protected abstract void encodeSoftCombineConstraint(CombineConstraint constraint);

    protected abstract void encodeSoftPreferenceConstraint(PreferenceConstraint constraint);

    public String encodeAndGetSoftConstraints(Collection<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            // Generate a soft constraint for subtype constraint
            if (constraint instanceof SubtypeConstraint) {
                encodeSoftSubtypeConstraint((SubtypeConstraint) constraint);
            }
            // Generate soft constraint for comparable constraint
            else if (constraint instanceof ComparableConstraint) {
                encodeSoftComparableConstraint((ComparableConstraint) constraint);
            }
            // Generate soft constraint for arithmetic constraint
            else if (constraint instanceof ArithmeticConstraint) {
                encodeSoftArithmeticConstraint((ArithmeticConstraint) constraint);
            }
            // Generate soft constraint for equality constraint
            else if (constraint instanceof EqualityConstraint) {
                encodeSoftEqualityConstraint((EqualityConstraint) constraint);
            }
            // Generate soft constraint for inequality constraint
            else if (constraint instanceof InequalityConstraint) {
                encodeSoftInequalityConstraint((InequalityConstraint) constraint);
            }
            // Generate soft constraint for implication constraint
            else if (constraint instanceof ImplicationConstraint) {
                encodeSoftImplicationConstraint((ImplicationConstraint) constraint);
            }
            // Generate soft constraint for existential constraint
            else if (constraint instanceof ExistentialConstraint) {
                encodeSoftExistentialConstraint((ExistentialConstraint) constraint);
            }
            // Generate soft constraint for combine constraint
            else if (constraint instanceof CombineConstraint) {
                encodeSoftCombineConstraint((CombineConstraint) constraint);
            }
            // Generate soft constraint for preference constraint
            else if (constraint instanceof PreferenceConstraint) {
                encodeSoftPreferenceConstraint((PreferenceConstraint) constraint);
            }
        }
        return softConstraints.toString();
    }
}
