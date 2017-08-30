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
import org.checkerframework.javacutil.AnnotationUtils;

import checkers.inference.DefaultInferenceSolution;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.solver.GeneralSolver;
import checkers.inference.solver.backend.BackEnd;
import checkers.inference.solver.backend.Translator;
import checkers.inference.solver.constraintgraph.ConstraintGraph;
import checkers.inference.solver.constraintgraph.GraphBuilder;
import checkers.inference.solver.constraintgraph.Vertex;
import checkers.inference.solver.frontend.LatticeBuilder;
import checkers.inference.solver.frontend.TwoQualifiersLattice;
import checkers.inference.solver.util.PrintUtils;
import checkers.inference.solver.util.StatisticRecorder;
import checkers.inference.solver.util.StatisticRecorder.StatisticKey;
import dataflow.DataflowAnnotatedTypeFactory;
import dataflow.qual.DataFlow;
import dataflow.qual.DataFlowInferenceBottom;
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

/**
 * DataflowGeneralSolver is the solver for dataflow type system. It encode
 * dataflow type hierarchy as two qualifiers type system.
 * 
 * @author jianchu
 *
 */
public class DataflowGeneralSolver extends GeneralSolver {

    private AnnotationMirror DATAFLOW;
    private AnnotationMirror DATAFLOWBOTTOM;
    private ProcessingEnvironment processingEnvironment;

    @Override
    protected InferenceSolution graphSolve(ConstraintGraph constraintGraph,
            Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, Translator<?, ?, ?> defaultTranslator) {

        DATAFLOW = AnnotationUtils.fromClass(processingEnvironment.getElementUtils(), DataFlow.class);
        DATAFLOWBOTTOM = AnnotationUtils.fromClass(processingEnvironment.getElementUtils(),
                DataFlowInferenceBottom.class);

        List<BackEnd<?, ?, ?>> backEnds = new ArrayList<>();
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
                    Translator<?, ?, ?> translator = createTranslator(backEndType, latticeFor2);
                    backEnds.add(createBackEnd(backEndType, configuration, slots, entry.getValue(),
                            qualHierarchy, processingEnvironment, latticeFor2, translator));
                } else if (dataflowRoots.length == 1) {
                    AnnotationMirror DATAFLOWTOP = DataflowUtils.createDataflowAnnotationForByte(
                            DataflowUtils.convert(dataflowRoots), processingEnvironment);
                    TwoQualifiersLattice latticeFor2 = new LatticeBuilder().buildTwoTypeLattice(DATAFLOWTOP, DATAFLOWBOTTOM);
                    Translator<?, ?, ?> translator = createTranslator(backEndType, latticeFor2);
                    backEnds.add(createBackEnd(backEndType, configuration, slots, entry.getValue(),
                            qualHierarchy, processingEnvironment, latticeFor2, translator));
                }
            }
        }
        return mergeSolution(solve(backEnds));
    }

    @Override
    protected ConstraintGraph generateGraph(Collection<Slot> slots, Collection<Constraint> constraints,
            ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        AnnotationMirror DATAFLOWTOP = AnnotationUtils.fromClass(
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
                if (AnnotationUtils.areSameIgnoringValues(dataflowAnno, DATAFLOW)) {
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

    @Override
    protected void sanitizeConfiguration() {
        if (!useGraph) {
            useGraph = true;
            InferenceMain.getInstance().logger
                    .warning("DataflowConstraintSolver: Solving constraint without graph will "
                            + "cause wrong answers in Dataflow type system. Modified solver argument \"useGraph\" to true.");
        }
    }

}
