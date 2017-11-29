package checkers.inference.solver.backend;

import checkers.inference.model.Constraint;

import java.util.Collection;

/**
 * Created by mier on 28/11/17.
 */
public interface FailureExplainer {

    Collection<Constraint> minimumUnsatisfiableConstraints();

}
