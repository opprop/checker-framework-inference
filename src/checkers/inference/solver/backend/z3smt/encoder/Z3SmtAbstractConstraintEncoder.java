package checkers.inference.solver.backend.z3smt.encoder;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoder;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

/** Abstract base class for every Z3Int constraint encoders. */
public class Z3SmtAbstractConstraintEncoder<SlotEncodingT, SlotSolutionT>
        extends AbstractConstraintEncoder<BoolExpr> {

    protected final Context ctx;
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
