package checkers.inference.solver.backend.strategy;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.frontend.LatticeBuilder;
import checkers.inference.solver.util.Constants.SolverArg;

public class PlainSolveStrategy extends AbstractSolveStrategy implements SolveStrategy {

    public PlainSolveStrategy(SolverFactory solverFactory) {
        super(solverFactory);
    }

    @Override
    public InferenceSolution solve(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        //TODO: Refactor way of parsing configuration.
        String solverName = configuration.get(SolverArg.solver.name());
        solverName = solverName == null ? "maxsat" : solverName;

        Lattice lattice = new LatticeBuilder().buildLattice(qualHierarchy, slots);

        FormatTranslator<?, ?, ?> formatTranslator = solverFactory.createFormatTranslator(solverName, lattice);
        SolverAdapter<?> underlyingSolver = solverFactory.createSolverAdapter(solverName, configuration,
                slots, constraints, processingEnvironment, lattice, formatTranslator);

        Map<Integer, AnnotationMirror> result = underlyingSolver.solve();

        return new DefaultInferenceSolution(result);
    }


}
