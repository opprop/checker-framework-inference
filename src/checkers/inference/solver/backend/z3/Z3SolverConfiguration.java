package checkers.inference.solver.backend.z3;

import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverConfiguration;

public class Z3SolverConfiguration implements SolverConfiguration {
    @Override
    public Class<? extends SolverAdapter<?>> getSolverAdapterClass() {
        return Z3Solver.class;
    }

    @Override
    public Class<? extends FormatTranslator<?,?,?>> getFormatTranslatorClass() {
        return Z3BitVectorFormatTranslator.class;
    };
}
