package checkers.inference.solver.backend;

public interface SolverConfiguration {

    Class<? extends SolverAdapter<?>> getSolverAdapterClass();

    Class<? extends FormatTranslator<?, ?, ?>> getFormatTranslatorClass();
}
