package checkers.inference.solver.backend.lingeling;

import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverConfiguration;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;

public class LingelingSolverConfiguration implements SolverConfiguration {

    @Override
    public Class<? extends SolverAdapter<?>> getSolverAdapterClass() {
        return LingelingSolver.class;
    }

    @Override
    public Class<? extends FormatTranslator<?, ?, ?>> getFormatTranslatorClass() {
        return MaxSatFormatTranslator.class;
    }

}
