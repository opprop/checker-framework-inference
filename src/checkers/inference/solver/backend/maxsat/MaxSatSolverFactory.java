package checkers.inference.solver.backend.maxsat;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class MaxSatSolverFactory implements SolverFactory {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            FormatTranslator<?, ?, ?> formatTranslator) {
        return new MaxSatSolver(solverOptions, slots, constraints, processingEnvironment, (MaxSatFormatTranslator) formatTranslator, lattice);
    }

    @Override
    public FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice,
            ConstraintVerifier verifier) {
        return new MaxSatFormatTranslator(lattice, verifier);
    }

}
