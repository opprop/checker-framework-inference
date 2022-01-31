package checkers.inference.solver.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

import checkers.inference.DefaultInferenceResult;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceResult;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.SolverEngine.SolverEngineArg;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverArg;
import checkers.inference.solver.util.SolverEnvironment;
import checkers.inference.solver.util.Statistics;
import com.sun.tools.javac.util.Pair;
import org.checkerframework.framework.type.QualifierHierarchy;

/**
 * GraphSolvingStrategy solves a given set of constraints by a divide-and-conquer way:
 *
 * 1. Build a {@link ConstraintGraph} based on the given set of constraints.
 * 2. Divide the constraint graph to multiple sub-graphs.
 * 3. For each sub-graphs, assign an underlying solver to solve it.
 * 4. Merge solutions of sub-graphs to get the final solution.
 *
 * This solving strategy is useful when solving constraints for a type system with a huge number of qualifers.
 * Normal plain solving strategy meet exponentially increased solving time in this case.
 */
public class GraphSolvingStrategy extends AbstractSolvingStrategy {

    enum GraphSolveStrategyArg implements SolverArg {
        solveInParallel;
    }

    public GraphSolvingStrategy(SolverFactory solverFactory) {
        super(solverFactory);
    }

    @Override
    public InferenceResult solve(SolverEnvironment solverEnvironment, Collection<Slot> slots,
                                 Collection<Constraint> constraints, Lattice lattice) {

        //TODO: Remove the coupling of using SolverEngineArg.
        final boolean solveInParallel = !"lingeling".equals(solverEnvironment.getArg(SolverEngineArg.solver))
                && solverEnvironment.getBoolArg(GraphSolveStrategyArg.solveInParallel);

        // Build graph
        final long graphBuildingStart = System.currentTimeMillis();
        ConstraintGraph constraintGraph = generateGraph(slots, constraints, solverEnvironment.processingEnvironment);
        final long graphBuildingEnd = System.currentTimeMillis();

        // Separate constraint graph, and assign each separated sub-graph to a underlying solver to solve.
        List<Solver<?>> separatedGraphSolvers = separateGraph(solverEnvironment, constraintGraph,
                slots, constraints, lattice);

        // Solving.
        List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> inferenceResults = new LinkedList<>();

        if (separatedGraphSolvers.size() > 0) {
            if (solveInParallel) {
                try {
                    inferenceResults = solveInparallel(separatedGraphSolvers);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                inferenceResults = solveInSequential(separatedGraphSolvers);
            }
        }

        // Merge solutions.
        InferenceResult result = mergeInferenceResults(inferenceResults);

        Statistics.addOrIncrementEntry("graph_generation_time(ms)", (graphBuildingEnd - graphBuildingStart));
        Statistics.addOrIncrementEntry("graph_size", constraintGraph.getIndependentPath().size());

        return result;
    }

    /**
     * Get the top qualifier in the underlying type hierarchy, which is used in simplifying the
     * constraint graph. By default, the first (and usually the only) annotation in the top
     * annotation list is returned.
     *
     * <p>This method should be overridden in special cases, e.g, type systems with multiple
     * type hierarchies.
     * @return the top annotation of the constraint graph
     */
    protected AnnotationMirror getGraphTopAnnotation() {
        QualifierHierarchy qualHierarchy = InferenceMain.getInstance().getRealTypeFactory().getQualifierHierarchy();
        return qualHierarchy.getTopAnnotations().iterator().next();
    }

    protected ConstraintGraph generateGraph(Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment) {
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints, getGraphTopAnnotation());
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();
        return constraintGraph;
    }

    /**
     * Separate constraint graph, and build an underlying solver for each separated sub-graph.
     *
     * Sub-class may customize their own way of separating the constraint graph by overriding this method.
     *
     * @return a list of underlying solvers, each of them responsible for solving a separated
     * sub-graph from the given constraint graph.
     */
    protected List<Solver<?>> separateGraph(SolverEnvironment solverEnvironment, ConstraintGraph constraintGraph,
            Collection<Slot> slots, Collection<Constraint> constraints, Lattice lattice) {
        List<Solver<?>> separatedGraphSovlers = new ArrayList<>();

        for (Set<Constraint> independentConstraints : constraintGraph.getIndependentPath()) {
            separatedGraphSovlers.add(solverFactory.createSolver(solverEnvironment, slots, independentConstraints, lattice));
        }

        return separatedGraphSovlers;
    }

    /**
     * This method is called if user wants to call all underlying solvers in parallel.
     *
     * @param underlyingSolvers
     * @return A list of Map that contains solutions from all underlying solvers.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    protected List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> solveInparallel(List<Solver<?>> underlyingSolvers)
            throws InterruptedException, ExecutionException {

        ExecutorService service = Executors.newFixedThreadPool(30);
        List<Future<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>>> futures = new ArrayList<>();

        long solvingStart = System.currentTimeMillis();
        for (final Solver<?> underlyingSolver : underlyingSolvers) {
            Callable<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> callable = () -> {
                Map<Integer, AnnotationMirror> solution = underlyingSolver.solve();
                if (solution != null) {
                    return new Pair<>(solution, new HashSet<>());
                } else {
                    return new Pair<>(solution, underlyingSolver.explainUnsatisfiable());
                }
            };
            futures.add(service.submit(callable));
        }
        service.shutdown();

        List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> results = new ArrayList<>();

        for (Future<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> future : futures) {
            results.add(future.get());
        }
        long solvingEnd = System.currentTimeMillis();

        Statistics.addOrIncrementEntry("overall_parallel_solving_time(ms)", (solvingEnd - solvingStart));
        return results;
    }

    /**
     * This method is called if user wants to call all underlying solvers in sequence.
     *
     * @param underlyingSolvers
     * @return A list of Map that contains solutions from all underlying solvers.
     */
    protected List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> solveInSequential(List<Solver<?>> underlyingSolvers) {

        List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> results = new ArrayList<>();

        long solvingStart = System.currentTimeMillis();
        for (final Solver<?> underlyingSolver : underlyingSolvers) {
            Map<Integer, AnnotationMirror> solution = underlyingSolver.solve();
            if (solution != null) {
                results.add(new Pair<>(solution, new HashSet<>()));
            } else {
                results.add(new Pair<>(solution, underlyingSolver.explainUnsatisfiable()));
            }
        }
        long solvingEnd = System.currentTimeMillis();

        Statistics.addOrIncrementEntry("overall_sequential_solving_time(ms)", (solvingEnd - solvingStart));
        return results;
    }

    /**
     * This method merges all solutions from all underlying solvers.
     *
     * @param inferenceResults
     * @return an InferenceResult for the given slots/constraints
     */
    protected InferenceResult mergeInferenceResults(List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> inferenceResults) {

        Map<Integer, AnnotationMirror> solutions = new HashMap<>();

        for (Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>> inferenceResult : inferenceResults) {
            if (inferenceResult.fst != null) {
                solutions.putAll(inferenceResult.fst);
            } else {
                // If any solution is null, there is no solution in whole. In this case, return the
                // unsolvable set of constraints for the current sub graph as explanations
                return new DefaultInferenceResult(inferenceResult.snd);
            }
        }

        // Till this point, there must be solution
        Statistics.addOrIncrementEntry("annotation_size", solutions.size());
        return new DefaultInferenceResult(solutions);
    }
}
