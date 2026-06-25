package checkers.inference.solver.backend.javasmt;

import java.util.Collection;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverEnvironment;

/** Factory for the JavaSMT backend. */
public class JavaSmtSolverFactory extends AbstractSolverFactory<JavaSmtFormatTranslator> {

    @Override
    public Solver<?> createSolver(
            SolverEnvironment solverEnvironment,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            Lattice lattice) {
        JavaSmtFormatTranslator formatTranslator = createFormatTranslator(lattice);
        return new JavaSmtSolver(solverEnvironment, slots, constraints, formatTranslator, lattice);
    }

    @Override
    protected JavaSmtFormatTranslator createFormatTranslator(Lattice lattice) {
        return new JavaSmtFormatTranslator(lattice);
    }
}
