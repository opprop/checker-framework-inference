package checkers.inference.solver.backend.maxsat;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class MaxSatSolverFactory extends AbstractSolverFactory<MaxSatFormatTranslator> {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            ConstraintVerifier verifier) {
        MaxSatFormatTranslator formatTranslator = createFormatTranslator(lattice, verifier);
        return new MaxSatSolver(solverOptions, slots, constraints, processingEnvironment, formatTranslator, lattice);
    }

    @Override
    public MaxSatFormatTranslator createFormatTranslator(Lattice lattice, ConstraintVerifier verifier) {
        return new MaxSatFormatTranslator(lattice, verifier);
    }

}
