package checkers.inference.solver.backend.z3smt;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;
import java.util.Collection;

import checkers.inference.solver.backend.z3smt.Z3SmtFormatTranslator;
import checkers.inference.solver.backend.z3smt.Z3SmtSolver;

public abstract class Z3SmtSolverFactory<SlotEncodingT, SlotSolutionT>
        extends AbstractSolverFactory<Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT>> {

    @Override
    public Solver<?> createSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Lattice lattice) {
        Z3SmtFormatTranslator<SlotEncodingT, SlotSolutionT> formatTranslator =
                createFormatTranslator(lattice);
        return new Z3SmtSolver<SlotEncodingT, SlotSolutionT>(
                solverEnvironment, slots, constraints, formatTranslator, lattice);
    }
}
