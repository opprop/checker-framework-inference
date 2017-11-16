package checkers.inference.solver.backend;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverArg;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public interface SolverFactory {

    public enum SolverFactoryArg implements SolverArg {
        /**
         * Name of the solver.
         */
        solver;

        @Override
        public String getName() {
            return this.name();
        }
    }

    Solver<?> createSolver(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment, Lattice lattice,
            FormatTranslator<?, ?, ?> formatTranslator);

    FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice, ConstraintVerifier verifier);
}
