package sparta.checkers.sat;

import checkers.inference.InferenceResult;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;
import sparta.checkers.iflow.util.IFlowUtils;
import sparta.checkers.iflow.util.PFPermission;
import sparta.checkers.qual.PolySink;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by smillst on 9/21/15.
 */
public class SinkSolver extends IFlowSolver {

    protected IFlowUtils flowUtils;

    @Override
    public InferenceResult solve(
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment
    ) {
        flowUtils = new IFlowUtils(processingEnvironment);
        return super.solve(configuration, slots, constraints, qualHierarchy, processingEnvironment);
    }

    @Override
    protected Set<PFPermission> getPermissionList(AnnotationMirror anno) {
        if (IFlowUtils.isPolySink(anno)) {
            return new HashSet<>();
        }
        return flowUtils.getSinks(anno);
    }

    @Override
    protected IFlowSerializer getSerializer(PFPermission permission) {
        return new SinkSerializer(permission);
    }

    @Override
    protected InferenceResult getMergedResultFromSolutions(ProcessingEnvironment processingEnvironment, List<PermissionSolution> solutions) {
        return new SinkResult(solutions, processingEnvironment);
    }
}
