package checkers.inference.solver.backend.z3smt.encoder;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

/** Abstract base class for every Z3Int constraint encoders. */
public abstract class Z3SmtAbstractConstraintEncoder<SlotEncodingT, SlotSolutionT>
        extends AbstractConstraintEncoder<BoolExpr> {

    protected final Context ctx;

    /**
     * {@link Z3SmtFormatTranslator} instance that concrete subclass of {@link
     * AbstractConstraintEncoder} might need. For example, {@link
     * checkers.inference.solver.backend.z3.encoder.Z3SmtSubtypeConstraintEncoder} needs it to
     * format translate {@SubtypeConstraint}. {@link
     * checkers.inference.solver.backend.maxsat.encoder.MaxSATImplicationConstraintEncoder} needs it
     * to delegate format translation task of non-{@code ImplicationConstraint}s.
     */
    protected final Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator;

    public Z3SmtAbstractConstraintEncoder(
            Lattice lattice,
            Context ctx,
            Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator) {
        // empty value is z3True, contradictory value is z3False
        super(lattice, ctx.mkTrue(), ctx.mkFalse());
        this.ctx = ctx;
        this.z3SmtFormatTranslator = z3SmtFormatTranslator;
    }
}
