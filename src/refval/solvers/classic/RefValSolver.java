package refval.solvers.classic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.AnnotationBuilder;

import checkers.inference.InferenceResult;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.constraintgraph.Vertex;
import refval.qual.RefVal;
import refval.util.RefValUtils;

/**
 * A solver for refval type system that is independent from GeneralSolver.
 *
 * @author jianchu
 *
 */
public class RefValSolver implements InferenceSolver {

    protected AnnotationMirror REFVAL;

    @Override
    public InferenceResult solve(Map<String, String> configuration,
                                 Collection<Slot> slots, Collection<Constraint> constraints,
                                 QualifierHierarchy qualHierarchy,
                                 ProcessingEnvironment processingEnvironment) {

        Elements elements = processingEnvironment.getElementUtils();
        REFVAL = AnnotationBuilder.fromClass(elements, RefVal.class);
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints);
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();

        List<RefValTypeSolver> refValSolvers = new ArrayList<>();

        // Configure datatype solvers
        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getConstantPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameByName(anno, REFVAL)) {
                String[] refValValues = RefValUtils.getTypeNames(anno);
                String[] refValRoots = RefValUtils.getTypeNameRoots(anno);
                if (refValValues.length == 1) {
                    RefValTypeSolver solver = new RefValTypeSolver(refValValues[0], entry.getValue(),getSerializer(refValValues[0], false));
                    refValSolvers.add(solver);
                } else if (refValRoots.length == 1) {
                    RefValTypeSolver solver = new RefValTypeSolver(refValRoots[0], entry.getValue(),getSerializer(refValRoots[0], true));
                    refValSolvers.add(solver);
                }
            }
        }

        List<RefValTypeSolution> solutions = new ArrayList<>();
        try {
            if (refValSolvers.size() > 0) {
                solutions = solveInparallel(refValSolvers);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return getMergedResultFromSolutions(processingEnvironment, solutions);
    }

    private List<RefValTypeSolution> solveInparallel(List<RefValTypeSolver> refValSolvers)
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(refValSolvers.size());

        List<Future<RefValTypeSolution>> futures = new ArrayList<Future<RefValTypeSolution>>();

        for (final RefValTypeSolver solver : refValSolvers) {
            Callable<RefValTypeSolution> callable = new Callable<RefValTypeSolution>() {
                @Override
                public RefValTypeSolution call() throws Exception {
                    return solver.solve();
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<RefValTypeSolution> solutions = new ArrayList<>();
        for (Future<RefValTypeSolution> future : futures) {
            solutions.add(future.get());
        }
        return solutions;
    }

    protected RefValSerializer getSerializer(String datatype, boolean isRoot) {
        return new RefValSerializer(datatype, isRoot);
    }

    protected InferenceResult getMergedResultFromSolutions(ProcessingEnvironment processingEnvironment,
                                                           List<RefValTypeSolution> solutions) {
        return new RefValResult(solutions, processingEnvironment);
    }
}
