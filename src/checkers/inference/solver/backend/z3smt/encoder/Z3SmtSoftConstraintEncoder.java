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
    
    public String encodeAndGetSoftConstraints(Collection<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            // Generate a soft constraint for subtype constraint
            if (constraint instanceof SubtypeConstraint) {
                encodeSubtypeConstraint((SubtypeConstraint) constraint);
            }
            // Generate soft constraint for comparison constraint
            if (constraint instanceof ComparableConstraint) {
                encodeComparableConstraint((ComparableConstraint) constraint);
            }
            // Generate soft constraint for arithmetic constraint
            if (constraint instanceof ArithmeticConstraint) {
                encodeArithmeticConstraint((ArithmeticConstraint) constraint);
            }
            // Generate soft constraint for equality constraint
            if (constraint instanceof EqualityConstraint) {
                encodeEqualityConstraint((EqualityConstraint) constraint);
            }
            // Generate soft constraint for inequality constraint
            if (constraint instanceof InequalityConstraint) {
                encodeInequalityConstraint((InequalityConstraint) constraint);
            }
            // Generate soft constraint for implication constraint
            if (constraint instanceof ImplicationConstraint) {
                encodeImplicationConstraint((ImplicationConstraint) constraint);
            }
            // Generate soft constraint for existential constraint
            if (constraint instanceof ExistentialConstraint) {
                encodeExistentialConstraint((ExistentialConstraint) constraint);
            }
            // Generate soft constraint for combine constraint
            if (constraint instanceof CombineConstraint) {
                encodeCombineConstraint((CombineConstraint) constraint);
            }
            // Generate soft constraint for preference constraint
            if (constraint instanceof PreferenceConstraint) {
                encodePreferenceConstraint((PreferenceConstraint) constraint);
            }
        }
        return softConstraints.toString();
    }
}
