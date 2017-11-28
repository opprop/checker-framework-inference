package checkers.inference.solver.backend.z3;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public abstract class Z3SolverFactory extends AbstractSolverFactory<Z3BitVectorFormatTranslator> {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            ConstraintVerifier verifier) {
        Z3BitVectorFormatTranslator formatTranslator = createFormatTranslator(lattice, verifier);
        return new Z3Solver(solverOptions, slots, constraints, processingEnvironment, formatTranslator, lattice);
    }
}
