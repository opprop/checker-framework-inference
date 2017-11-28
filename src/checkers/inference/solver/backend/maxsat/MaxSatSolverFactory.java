package checkers.inference.solver.backend.maxsat;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;

public class MaxSatSolverFactory extends AbstractSolverFactory<MaxSatFormatTranslator> {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice) {
        MaxSatFormatTranslator formatTranslator = createFormatTranslator(lattice);
        return new MaxSatSolver(solverOptions, slots, constraints, processingEnvironment, formatTranslator, lattice);
    }

    @Override
    public MaxSatFormatTranslator createFormatTranslator(Lattice lattice) {
        return new MaxSatFormatTranslator(lattice);
    }

}
