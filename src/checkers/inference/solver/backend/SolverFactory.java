package checkers.inference.solver.backend;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;

public interface SolverFactory {

    public enum SolverFactoryArg {
        /**
         * Name of the solver.
         */
        solver;
    }

    SolverAdapter<?> createSolverAdapter(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment, Lattice lattice,
            FormatTranslator<?, ?, ?> formatTranslator);

    FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice);
}
