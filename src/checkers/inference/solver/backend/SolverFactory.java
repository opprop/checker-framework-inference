package checkers.inference.solver.backend;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;

public interface SolverFactory {

    SolverAdapter<?> createSolverAdapter(String solverType, Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment, Lattice lattice,
            FormatTranslator<?, ?, ?> formatTranslator);

    FormatTranslator<?, ?, ?> createFormatTranslator(String solverType, Lattice lattice);
}
