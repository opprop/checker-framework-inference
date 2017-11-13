package dataflow.solvers.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.FormatTranslator;
import checkers.inference.solver.backend.SolverAdapter;
import checkers.inference.solver.backend.SolverFactory;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.constraintgraph.Vertex;
import checkers.inference.solver.frontend.LatticeBuilder;
import checkers.inference.solver.frontend.TwoQualifiersLattice;
import checkers.inference.solver.strategy.GraphSolveStrategy;
import checkers.inference.solver.util.PrintUtils;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.solver.util.StatisticRecorder;
import checkers.inference.solver.util.StatisticRecorder.StatisticKey;
import dataflow.DataflowAnnotatedTypeFactory;
import dataflow.qual.DataFlow;
import dataflow.qual.DataFlowInferenceBottom;
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

public class DataflowGraphSolveStrategy extends GraphSolveStrategy {

    protected ProcessingEnvironment processingEnvironment;

    public DataflowGraphSolveStrategy(SolverFactory solverFactory) {
        super(solverFactory);
    }

    @Override
    public InferenceSolution solve(SolverOptions solverOptions, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        return super.solve(solverOptions, slots, constraints, qualHierarchy, processingEnvironment);
    }

    @Override
    protected List<SolverAdapter<?>> separateGraph(ConstraintGraph constraintGraph, SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy, ProcessingEnvironment processingEnvironment) {
        AnnotationMirror DATAFLOW = AnnotationBuilder.fromClass(processingEnvironment.getElementUtils(), DataFlow.class);
        AnnotationMirror DATAFLOWBOTTOM = AnnotationBuilder.fromClass(processingEnvironment.getElementUtils(),
                DataFlowInferenceBottom.class);

        List<SolverAdapter<?>> solvers = new ArrayList<>();
        //TODO: Refactor statistic part.
        StatisticRecorder.record(StatisticKey.GRAPH_SIZE, (long) constraintGraph.getConstantPath()
                .size());

        for (Map.Entry<Vertex, Set<Constraint>> entry : constraintGraph.getConstantPath().entrySet()) {
            AnnotationMirror anno = entry.getKey().getValue();
            if (AnnotationUtils.areSameIgnoringValues(anno, DATAFLOW)) {
                String[] dataflowValues = DataflowUtils.getTypeNames(anno);
                String[] dataflowRoots = DataflowUtils.getTypeNameRoots(anno);
                if (dataflowValues.length == 1) {
                    AnnotationMirror DATAFLOWTOP = DataflowUtils.createDataflowAnnotation(
                            DataflowUtils.convert(dataflowValues), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new LatticeBuilder().buildTwoTypeLattice(DATAFLOWTOP, DATAFLOWBOTTOM);
                    FormatTranslator<?, ?, ?> translator = solverFactory.createFormatTranslator(solverOptions, latticeFor2);
                    solvers.add(solverFactory.createSolverAdapter(solverOptions, slots, entry.getValue(),
                            processingEnvironment, latticeFor2, translator));
                } else if (dataflowRoots.length == 1) {
                    AnnotationMirror DATAFLOWTOP = DataflowUtils.createDataflowAnnotationForByte(
                            DataflowUtils.convert(dataflowRoots), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new LatticeBuilder().buildTwoTypeLattice(DATAFLOWTOP, DATAFLOWBOTTOM);
                    FormatTranslator<?, ?, ?> translator = solverFactory.createFormatTranslator(solverOptions, latticeFor2);
                    solvers.add(solverFactory.createSolverAdapter(solverOptions, slots, entry.getValue(),
                            processingEnvironment, latticeFor2, translator));
                }
            }
        }

        return solvers;
    }

    @Override
    protected ConstraintGraph generateGraph(Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment) {
        AnnotationMirror DATAFLOWTOP = AnnotationBuilder.fromClass(
                processingEnvironment.getElementUtils(), DataFlowTop.class);
        GraphBuilder graphBuilder = new GraphBuilder(slots, constraints, DATAFLOWTOP);
        ConstraintGraph constraintGraph = graphBuilder.buildGraph();
        return constraintGraph;
    }

    @Override
    protected InferenceSolution mergeSolution(List<Map<Integer, AnnotationMirror>> inferenceSolutionMaps) {
        Map<Integer, AnnotationMirror> result = new HashMap<>();
        Map<Integer, Set<AnnotationMirror>> dataflowResults = new HashMap<>();

        for (Map<Integer, AnnotationMirror> inferenceSolutionMap : inferenceSolutionMaps) {
            for (Map.Entry<Integer, AnnotationMirror> entry : inferenceSolutionMap.entrySet()) {
                Integer id = entry.getKey();
                AnnotationMirror dataflowAnno = entry.getValue();
                if (AnnotationUtils.areSameByClass(dataflowAnno, DataFlow.class)) {
                    Set<AnnotationMirror> datas = dataflowResults.get(id);
                    if (datas == null) {
                        datas = AnnotationUtils.createAnnotationSet();
                        dataflowResults.put(id, datas);
                    }
                    datas.add(dataflowAnno);
                }
            }

        }
        for (Map.Entry<Integer, Set<AnnotationMirror>> entry : dataflowResults.entrySet()) {
            Set<String> dataTypes = new HashSet<String>();
            Set<String> dataRoots = new HashSet<String>();
            for (AnnotationMirror anno : entry.getValue()) {
                String[] dataTypesArr = DataflowUtils.getTypeNames(anno);
                String[] dataRootsArr = DataflowUtils.getTypeNameRoots(anno);
                if (dataTypesArr.length == 1) {
                    dataTypes.add(dataTypesArr[0]);
                }
                if (dataRootsArr.length == 1) {
                    dataRoots.add(dataRootsArr[0]);
                }
            }
            AnnotationMirror dataflowAnno = DataflowUtils.createDataflowAnnotationWithRoots(dataTypes,
                    dataRoots, processingEnvironment);
            result.put(entry.getKey(), dataflowAnno);
        }
        for (Map.Entry<Integer, AnnotationMirror> entry : result.entrySet()) {
            AnnotationMirror refinedDataflow = ((DataflowAnnotatedTypeFactory) InferenceMain
                    .getInstance().getRealTypeFactory()).refineDataflow(entry.getValue());
            entry.setValue(refinedDataflow);
        }

        PrintUtils.printResult(result);
        StatisticRecorder.record(StatisticKey.ANNOTATOIN_SIZE, (long) result.size());
        return new DefaultInferenceSolution(result);
    }
}
