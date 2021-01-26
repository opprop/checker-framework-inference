package refval.solvers.classic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceResult;
import checkers.inference.InferenceMain;
import checkers.inference.solver.util.PrintUtils;
import refval.RefValAnnotatedTypeFactory;
import refval.util.RefValUtils;

public class RefValResult extends DefaultInferenceResult {
    private final Map<Integer, Set<String>> typeNameResults;
    private final Map<Integer, Set<String>> typeRootResults;
    private final Map<Integer, Boolean> idToExistance;
    private final RefValAnnotatedTypeFactory realTypeFactory;

    public RefValResult(Collection<RefValTypeSolution> solutions, ProcessingEnvironment processingEnv) {
        // Legacy solver doesn't support explanation
        super();
        this.typeNameResults = new HashMap<>();
        this.typeRootResults = new HashMap<>();
        this.idToExistance = new HashMap<>();
        this.realTypeFactory = (RefValAnnotatedTypeFactory)InferenceMain.getInstance().getRealTypeFactory();
        mergeSolutions(solutions);
        createAnnotations(processingEnv);
        simplifyAnnotation();
        PrintUtils.printSolutions(varIdToAnnotation);
    }

    public void mergeSolutions(Collection<RefValTypeSolution> solutions) {
        for (RefValTypeSolution solution : solutions) {
            mergeSingleSolution(solution);
            mergeIdToExistance(solution);
        }
    }

    private void mergeSingleSolution(RefValTypeSolution solution) {
        for (Map.Entry<Integer, Boolean> entry : solution.getResult().entrySet()) {
            boolean shouldContainDatatype = shouldContainDatatype(entry);
            String datatype = solution.getRefValType();
            if (solution.isRoot()) {
                Set<String> dataRoots = typeRootResults.get(entry.getKey());
                if (dataRoots == null) {
                    dataRoots = new TreeSet<>();
                    typeRootResults.put(entry.getKey(), dataRoots);
                }
                if (shouldContainDatatype) {
                    dataRoots.add(datatype);
                }
            } else {
                Set<String> datatypes = typeNameResults.get(entry.getKey());
                if (datatypes == null) {
                    datatypes = new TreeSet<>();
                    typeNameResults.put(entry.getKey(), datatypes);
                }
                if (shouldContainDatatype) {
                    datatypes.add(datatype);
                }
            }
        }
    }

    protected boolean shouldContainDatatype(Map.Entry<Integer, Boolean> entry) {
        return entry.getValue();
    }

    private void createAnnotations(ProcessingEnvironment processingEnv) {
        for (Map.Entry<Integer, Set<String>> entry : typeNameResults.entrySet()) {
            int slotId = entry.getKey();
            Set<String> datatypes = entry.getValue();
            Set<String> roots = typeRootResults.get(slotId);
            AnnotationMirror anno;
            if (roots != null) {
                anno = RefValUtils.createRefValAnnotationWithRoots(datatypes, typeRootResults.get(slotId), processingEnv);
            } else {
                anno = RefValUtils.createRefValAnnotation(datatypes, processingEnv);
            }
            varIdToAnnotation.put(slotId, anno);
        }

        for (Map.Entry<Integer, Set<String>> entry : typeRootResults.entrySet()) {
            int slotId = entry.getKey();
            Set<String> roots = entry.getValue();
            Set<String> typeNames = typeNameResults.get(slotId);
            if (typeNames == null) {
                AnnotationMirror anno = RefValUtils
                    .createRefValAnnotationWithoutName(roots, processingEnv);
                varIdToAnnotation.put(slotId, anno);
            }
        }

    }

    private void simplifyAnnotation() {
        for (Map.Entry<Integer, AnnotationMirror> entry : varIdToAnnotation.entrySet()) {
            AnnotationMirror refinedRefVal = this.realTypeFactory.refineRefVal(entry.getValue());
            entry.setValue(refinedRefVal);
        }
    }

    private void mergeIdToExistance(RefValTypeSolution solution) {
        for (Map.Entry<Integer, Boolean> entry : solution.getResult().entrySet()) {
            int id = entry.getKey();
            boolean existsDatatype = entry.getValue();
            if (idToExistance.containsKey(id)) {
                boolean alreadyExists = idToExistance.get(id);
                if (alreadyExists ^ existsDatatype) {
                    InferenceMain.getInstance().logger.log(Level.INFO, "Mismatch between existance of annotation");
                }
            } else {
                idToExistance.put(id, existsDatatype);
            }
        }
    }

    // TODO
    // Mier: I'm worried that this causes inconsistency with getSolutionForVariable(), as it uses
    // a different map - varIdToAnnotation to store the actual var id to annotation solution.
    @Override
    public boolean containsSolutionForVariable(int varId) {
        return idToExistance.containsKey(varId);
    }

}
