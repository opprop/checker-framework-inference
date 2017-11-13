package checkers.inference.solver.backend;

public interface SolverAdapterInfo {

    Class<? extends SolverAdapter<?>> getSolverAdapterClass();

    Class<? extends FormatTranslator<?, ?, ?>> getFormatTranslatorClass();
}
