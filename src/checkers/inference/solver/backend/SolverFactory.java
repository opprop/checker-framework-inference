package checkers.inference.solver.backend;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.solver.SolverEngine;

/**
 * Factory class for creating an underlying solver.
 *
 * Note: subclass of this interface should have a zero parameter
 * constructor, and should follow the naming convention to let {@code SolverEngine}
 * reflectively load the subclass instance.
 *
 * Naming convention of solver factory for underlying solvers:
 *
 * Package name should be: checkers.inference.solver.backend.[(all lower cases)underlying solver name]
 * Under this package, create a subclass named: [underlying solver name]SolverFactory.
 *
 * E.g. For MaxSat solver:
 *
 * Package name: checkers.inference.solver.backend.maxsat
 * Class name: MaxSatSolverFactory
 *
 * @see SolverEngine#createSolverFactory()
 */
public interface SolverFactory {

    Solver<?> createSolver(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment, Lattice lattice);
}
