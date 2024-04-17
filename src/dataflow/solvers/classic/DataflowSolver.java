package dataflow.solvers.classic;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

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

import checkers.inference.InferenceResult;
import checkers.inference.InferenceSolver;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.constraintgraph.Vertex;
import dataflow.qual.DataFlow;
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

/**
 * A solver for dataflow type system that is independent from GeneralSolver.
 *
 * @author jianchu
 */
public class DataflowSolver implements InferenceSolver {

    protected AnnotationMirror DATAFLOW;

    protected DataflowUtils dataflowUtils;

    @Override
    public InferenceResult solve(
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        Elements elements = processingEnvironment.getElementUtils();
        DATAFLOW = AnnotationBuilder.fromClass(elements, DataFlow.class);
        dataflowUtils = new DataflowUtils(processingEnvironment);
        AnnotationMirror dataflowTop = AnnotationBuilder.fromClass(elements, DataFlowTop.class);
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints, dataflowTop);
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();

        List<DatatypeSolver> dataflowSolvers = new ArrayList<>();

        // Configure datatype solvers
        for (Map.Entry<Vertex, Set<Constraint>> entry :
                constraintGraph.getConstantPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameByName(anno, DATAFLOW)) {
                List<String> dataflowValues = dataflowUtils.getTypeNames(anno);
                List<String> dataflowRoots = dataflowUtils.getTypeNameRoots(anno);
                if (dataflowValues.size() == 1) {
                    String datatype = dataflowValues.get(0);
                    DatatypeSolver solver =
                            new DatatypeSolver(
                                    datatype, entry.getValue(), getSerializer(datatype, false));
                    dataflowSolvers.add(solver);
                } else if (dataflowRoots.size() == 1) {
                    String datatype = dataflowRoots.get(0);
                    DatatypeSolver solver =
                            new DatatypeSolver(
                                    datatype, entry.getValue(), getSerializer(datatype, true));
                    dataflowSolvers.add(solver);
                }
            }
        }

        List<DatatypeSolution> solutions = new ArrayList<>();
        try {
            if (dataflowSolvers.size() > 0) {
                solutions = solveInparallel(dataflowSolvers);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return getMergedResultFromSolutions(processingEnvironment, solutions);
    }

    private List<DatatypeSolution> solveInparallel(List<DatatypeSolver> dataflowSolvers)
            throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(dataflowSolvers.size());

        List<Future<DatatypeSolution>> futures = new ArrayList<Future<DatatypeSolution>>();

        for (final DatatypeSolver solver : dataflowSolvers) {
            Callable<DatatypeSolution> callable =
                    new Callable<DatatypeSolution>() {
                        @Override
                        public DatatypeSolution call() throws Exception {
                            return solver.solve();
                        }
                    };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<DatatypeSolution> solutions = new ArrayList<>();
        for (Future<DatatypeSolution> future : futures) {
            solutions.add(future.get());
        }
        return solutions;
    }

    protected DataflowSerializer getSerializer(String datatype, boolean isRoot) {
        return new DataflowSerializer(datatype, isRoot, dataflowUtils);
    }

    protected InferenceResult getMergedResultFromSolutions(
            ProcessingEnvironment processingEnvironment, List<DatatypeSolution> solutions) {
        return new DataflowResult(solutions, processingEnvironment);
    }
}
