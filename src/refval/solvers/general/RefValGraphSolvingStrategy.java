package refval.solvers.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceResult;
import com.sun.tools.javac.util.Pair;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import checkers.inference.InferenceMain;
import checkers.inference.InferenceResult;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.Solver;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.constraintgraph.Vertex;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.frontend.LatticeBuilder;
import checkers.inference.solver.frontend.TwoQualifiersLattice;
import checkers.inference.solver.strategy.GraphSolvingStrategy;
import checkers.inference.solver.util.SolverEnvironment;
import checkers.inference.solver.util.Statistics;
import refval.RefValAnnotatedTypeFactory;
import refval.qual.RefVal;
import refval.qual.BottomRefVal;
import refval.qual.UnknownRefVal;
import refval.util.RefValUtils;

public class RefValGraphSolvingStrategy extends GraphSolvingStrategy {

    protected ProcessingEnvironment processingEnvironment;

    public RefValGraphSolvingStrategy(SolverFactory solverFactory) {
        super(solverFactory);
    }

    @Override
    public InferenceResult solve(SolverEnvironment solverEnvironment, Collection<Slot> slots,
                                 Collection<Constraint> constraints, Lattice lattice) {
        this.processingEnvironment = solverEnvironment.processingEnvironment;
        return super.solve(solverEnvironment, slots, constraints, lattice);
    }

    @Override
    protected List<Solver<?>> separateGraph(SolverEnvironment solverEnvironment, ConstraintGraph constraintGraph,
            Collection<Slot> slots, Collection<Constraint> constraints, Lattice lattice) {
        AnnotationMirror REFVAL = AnnotationBuilder.fromClass(processingEnvironment.getElementUtils(), RefVal.class);
        AnnotationMirror BOTTOMREFVAL = AnnotationBuilder.fromClass(processingEnvironment.getElementUtils(),
                BottomRefVal.class);

        List<Solver<?>> solvers = new ArrayList<>();
        Statistics.addOrIncrementEntry("graph_size", constraintGraph.getConstantPath().size());

        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getConstantPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameByName(anno, REFVAL)) {
                String[] refValValues = RefValUtils.getTypeNames(anno);
                String[] refValRoots = RefValUtils.getTypeNameRoots(anno);
                if (refValValues.length == 1) {
                    AnnotationMirror UNKNOWNREFVAL = RefValUtils.createRefValAnnotation(
                            RefValUtils.convert(refValValues), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new LatticeBuilder().buildTwoTypeLattice(UNKNOWNREFVAL, BOTTOMREFVAL);
                    solvers.add(solverFactory.createSolver(solverEnvironment, slots, entry.getValue(), latticeFor2));
                } else if (refValRoots.length == 1) {
                    AnnotationMirror UNKNOWNREFVAL = RefValUtils.createRefValAnnotationForByte(
                            RefValUtils.convert(refValRoots), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new LatticeBuilder().buildTwoTypeLattice(UNKNOWNREFVAL, BOTTOMREFVAL);
                    solvers.add(solverFactory.createSolver(solverEnvironment, slots, entry.getValue(), latticeFor2));
                }
            }
        }

        return solvers;
    }

    @Override
    protected ConstraintGraph generateGraph(Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment) {
        AnnotationMirror UNKNOWNREFVAL = AnnotationBuilder.fromClass(
                processingEnvironment.getElementUtils(), UnknownRefVal.class);
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints, UNKNOWNREFVAL);
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();
        return constraintGraph;
    }

    @Override
    protected InferenceResult mergeInferenceResults(List<Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>>> inferenceResults) {
        Map<Integer, AnnotationMirror> solutions = new HashMap<>();
        Map<Integer, Set<AnnotationMirror>> refValResults = new HashMap<>();

        for (Pair<Map<Integer, AnnotationMirror>, Collection<Constraint>> inferenceResult : inferenceResults) {
            Map<Integer, AnnotationMirror> inferenceSolutionMap = inferenceResult.fst;
            if (inferenceResult.fst != null) {
                for (Map.Entry<Integer, AnnotationMirror> entry : inferenceSolutionMap.entrySet()) {
                    Integer id = entry.getKey();
                    AnnotationMirror refValAnno = entry.getValue();
                    if (AnnotationUtils.areSameByClass(refValAnno, RefVal.class)) {
                        Set<AnnotationMirror> datas = refValResults.get(id);
                        if (datas == null) {
                            datas = AnnotationUtils.createAnnotationSet();
                            refValResults.put(id, datas);
                        }
                        datas.add(refValAnno);
                    }
                }
            } else {
                // If any sub solution is null, there is no solution in a whole.
                return new DefaultInferenceResult(inferenceResult.snd);
            }
        }

        for (Map.Entry<Integer, Set<AnnotationMirror>> entry : refValResults.entrySet()) {
            Set<String> dataTypes = new HashSet<String>();
            Set<String> dataRoots = new HashSet<String>();
            for (AnnotationMirror anno : entry.getValue()) {
                String[] dataTypesArr = RefValUtils.getTypeNames(anno);
                String[] dataRootsArr = RefValUtils.getTypeNameRoots(anno);
                if (dataTypesArr.length == 1) {
                    dataTypes.add(dataTypesArr[0]);
                }
                if (dataRootsArr.length == 1) {
                    dataRoots.add(dataRootsArr[0]);
                }
            }
            AnnotationMirror refValAnno = RefValUtils.createRefValAnnotationWithRoots(dataTypes,
                    dataRoots, processingEnvironment);
            solutions.put(entry.getKey(), refValAnno);
        }
        for (Map.Entry<Integer, AnnotationMirror> entry : solutions.entrySet()) {
            AnnotationMirror refinedRefVal = ((RefValAnnotatedTypeFactory) InferenceMain
                    .getInstance().getRealTypeFactory()).refineRefVal(entry.getValue());
            entry.setValue(refinedRefVal);
        }

        Statistics.addOrIncrementEntry("annotation_size", solutions.size());

        return new DefaultInferenceResult(solutions);
    }
}
