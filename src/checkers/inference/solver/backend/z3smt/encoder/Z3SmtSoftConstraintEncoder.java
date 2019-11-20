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
import checkers.inference.solver.backend.encoder.AbstractConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

public abstract class Z3SmtSoftConstraintEncoder<SlotEncodingT, SlotSolutionT> {
	
	protected final Context ctx;
	
	/**
     * {@link Lattice} instance that every constraint encoder needs
     */
    protected final Lattice lattice;

    /**
     * {@link Z3SmtFormatTranslator} instance that concrete subclass of {@link AbstractConstraintEncoder} might need.
     * For example, {@link checkers.inference.solver.backend.z3.encoder.Z3SmtSubtypeConstraintEncoder} needs
     * it to format translate {@SubtypeConstraint}. {@link checkers.inference.solver.backend.maxsat.encoder.MaxSATImplicationConstraintEncoder}
     * needs it to delegate format translation task of non-{@code ImplicationConstraint}s.
     */
    protected final Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> formatTranslator;

	protected StringBuffer softConstraints;

    public Z3SmtSoftConstraintEncoder(
    	    Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator) {
    	this.lattice = lattice;
        this.formatTranslator = z3SmtFormatTranslator;
        this.ctx = ctx;
        this.softConstraints = new StringBuffer();
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

    protected void addSoftConstraint(Expr serializedConstraint, int weight) {
    	softConstraints.append("(assert-soft " + serializedConstraint + " :weight " + weight + ")\n");
    }
    
    public String encodeAndGetSoftConstraints(Collection<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            // Generate a soft constraint for subtype constraint
            if (constraint instanceof SubtypeConstraint) {
            	encodeSoftSubtypeConstraint((SubtypeConstraint) constraint);
            }
            // Generate soft constraint for comparison constraint
            if (constraint instanceof ComparableConstraint) {
            	encodeSoftComparableConstraint((ComparableConstraint) constraint);
            }
            // Generate soft constraint for arithmetic constraint
            if (constraint instanceof ArithmeticConstraint) {
            	encodeSoftArithmeticConstraint((ArithmeticConstraint) constraint);
            }
            // Generate soft constraint for equality constraint
            if (constraint instanceof EqualityConstraint) {
            	encodeSoftEqualityConstraint((EqualityConstraint) constraint);
            }
            // Generate soft constraint for inequality constraint
            if (constraint instanceof InequalityConstraint) {
            	encodeSoftInequalityConstraint((InequalityConstraint) constraint);
            }
            // Generate soft constraint for implication constraint
            if (constraint instanceof ImplicationConstraint) {
            	encodeSoftImplicationConstraint((ImplicationConstraint) constraint);
            }
            // Generate soft constraint for existential constraint
            if (constraint instanceof ExistentialConstraint) {
            	encodeSoftExistentialConstraint((ExistentialConstraint) constraint);
            }
            // Generate soft constraint for combine constraint
            if (constraint instanceof CombineConstraint) {
            	encodeSoftCombineConstraint((CombineConstraint) constraint);
            }
            // Generate soft constraint for preference constraint
            if (constraint instanceof PreferenceConstraint) {
            	encodeSoftPreferenceConstraint((PreferenceConstraint) constraint);
            }
        }
    	return softConstraints.toString();
    }
}
