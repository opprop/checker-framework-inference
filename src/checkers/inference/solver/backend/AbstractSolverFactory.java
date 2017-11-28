package checkers.inference.solver.backend;

import checkers.inference.solver.frontend.Lattice;

public abstract class AbstractSolverFactory<T extends FormatTranslator<?, ?, ?>> implements SolverFactory {

    abstract protected T createFormatTranslator(Lattice lattice);
}
