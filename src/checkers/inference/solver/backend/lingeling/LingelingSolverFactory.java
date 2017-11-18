package checkers.inference.solver.backend.lingeling;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class LingelingSolverFactory extends AbstractSolverFactory<MaxSatFormatTranslator> {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            ConstraintVerifier verifier) {
        MaxSatFormatTranslator formatTranslator = createFormatTranslator(lattice, verifier);
        return new LingelingSolver(solverOptions, slots, constraints, processingEnvironment, formatTranslator, lattice);
    }

    @Override
    protected MaxSatFormatTranslator createFormatTranslator(Lattice lattice,
            ConstraintVerifier verifier) {
           return new MaxSatFormatTranslator(lattice, verifier);
    }

}
