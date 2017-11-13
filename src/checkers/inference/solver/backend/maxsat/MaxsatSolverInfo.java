package checkers.inference.solver.backend.maxsat;

import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverAdapterInfo;

public class MaxsatSolverInfo implements SolverAdapterInfo {

    @Override
    public Class<? extends SolverAdapter<?>> getSolverAdapterClass() {
        return MaxSatSolver.class;
    }

    @Override
    public Class<? extends FormatTranslator<?, ?, ?>> getFormatTranslatorClass() {
        return MaxSatFormatTranslator.class;
    }

}
