package checkers.inference.solver.backend;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public abstract class AbstractSolverFactory<T extends FormatTranslator<?, ?, ?>> implements SolverFactory {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            ConstraintVerifier verifier) {
        // TODO Auto-generated method stub
        return null;
    }

    abstract protected T createFormatTranslator(Lattice lattice, ConstraintVerifier constraintVerifier);
}
