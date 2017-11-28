package checkers.inference.solver.backend.logiql;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.AbstractSolverFactory;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class LogiQLSolverFactory extends AbstractSolverFactory<LogiQLFormatTranslator> {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice) {
        LogiQLFormatTranslator formatTranslator = createFormatTranslator(lattice);
        return new LogiQLSolver(solverOptions, slots, constraints, processingEnvironment, formatTranslator, lattice);
    }

    @Override
    public LogiQLFormatTranslator createFormatTranslator(Lattice lattice) {
        return new LogiQLFormatTranslator(lattice);
    }

}
