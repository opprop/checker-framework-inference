package checkers.inference.solver.strategy;

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
import checkers.inference.solver.util.SolverOptions;

public class PlainSolveStrategy extends AbstractSolveStrategy implements SolveStrategy {

    public PlainSolveStrategy(SolverFactory solverFactory) {
        super(solverFactory);
    }

    @Override
    public InferenceSolution solve(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        Lattice lattice = new LatticeBuilder().buildLattice(qualHierarchy, slots);

        FormatTranslator<?, ?, ?> formatTranslator = solverFactory.createFormatTranslator(solverOptions, lattice);
        SolverAdapter<?> underlyingSolver = solverFactory.createSolverAdapter(solverOptions, slots, constraints,
                processingEnvironment, lattice, formatTranslator);

        Map<Integer, AnnotationMirror> result = underlyingSolver.solve();

        return new DefaultInferenceSolution(result);
    }


}
