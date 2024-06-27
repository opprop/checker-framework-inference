package sparta.checkers.sat;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceResult;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import sparta.checkers.iflow.util.IFlowUtils;
import sparta.checkers.iflow.util.PFPermission;

/** Created by smillst on 9/17/15. */
public class SourceSolver extends IFlowSolver {

    protected IFlowUtils flowUtils;

    @Override
    public InferenceResult solve(
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {
        flowUtils = new IFlowUtils(processingEnvironment);
        return super.solve(configuration, slots, constraints, qualHierarchy, processingEnvironment);
    }

    protected Set<PFPermission> getPermissionList(AnnotationMirror anno) {
        if (IFlowUtils.isPolySource(anno)) {
            return new HashSet<>();
        }
        return flowUtils.getSources(anno);
    }

    @Override
    protected IFlowSerializer getSerializer(PFPermission permission) {
        return new SourceSerializer(permission);
    }

    protected InferenceResult getMergedResultFromSolutions(
            ProcessingEnvironment processingEnvironment, List<PermissionSolution> solutions) {
        return new SourceResult(solutions, processingEnvironment);
    }
}
