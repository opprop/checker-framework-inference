package checkers.inference.solver.backend.strategy;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.framework.type.QualifierHierarchy;

import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.util.SolverOptions;

public interface SolveStrategy {

    InferenceSolution solve(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment);
}
