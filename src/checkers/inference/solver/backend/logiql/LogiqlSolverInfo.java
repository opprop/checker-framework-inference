package checkers.inference.solver.backend.logiql;

import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverAdapterInfo;

public class LogiqlSolverInfo implements SolverAdapterInfo {

    @Override
    public Class<? extends SolverAdapter<?>> getSolverAdapterClass() {
        return LogiQLSolver.class;
    }

    @Override
    public Class<? extends FormatTranslator<?, ?, ?>> getFormatTranslatorClass() {
        return LogiQLFormatTranslator.class;
    }

}
