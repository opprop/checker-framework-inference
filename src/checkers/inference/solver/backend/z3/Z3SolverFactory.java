package checkers.inference.solver.backend.z3;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class Z3SolverFactory implements SolverFactory {

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment, Lattice lattice,
            FormatTranslator<?, ?, ?> formatTranslator) {
        return new Z3Solver(solverOptions, slots, constraints, processingEnvironment, (Z3BitVectorFormatTranslator) formatTranslator, lattice);
    }

    @Override
    public FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice,
            ConstraintVerifier verifier) {
        ErrorReporter.errorAbort("Z3 doesn't have a default format translator for bit vectory theory, please provide your implementation!");
        return null;
    }

}
