package checkers.inference.solver.backend.z3smt.encoder;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

/**
 * Abstract Z3 implementation of {@link
 * checkers.inference.solver.backend.encoder.ConstraintEncoderFactory} for integer theory.
 * Subclasses must specify the exact encoders used.
 *
 * @see checkers.inference.solver.backend.encoder.ConstraintEncoderFactory
 */
public abstract class Z3SmtConstraintEncoderFactory<SlotEncodingT, SlotSolutionT>
    extends AbstractConstraintEncoderFactory<BoolExpr, Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT>> {
        protected final Context ctx;

        public Z3SmtConstraintEncoderFactory(
        	    Lattice lattice,
                Context ctx,
                Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> z3SmtFormatTranslator) {
            super(lattice, z3SmtFormatTranslator);
            this.ctx = ctx;
        }
}
