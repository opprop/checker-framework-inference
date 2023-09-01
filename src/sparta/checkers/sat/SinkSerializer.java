package sparta.checkers.sat;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import sparta.checkers.iflow.util.IFlowUtils;
import sparta.checkers.iflow.util.PFPermission;

/** Created by smillst on 9/21/15. */
public class SinkSerializer extends IFlowSerializer {

    protected final IFlowUtils flowUtils;

    public SinkSerializer(PFPermission permission) {
        super(permission);
        flowUtils =
                new IFlowUtils(InferenceMain.getInstance().getRealTypeFactory().getProcessingEnv());
    }

    @Override
    public boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return !annoHasPermission(anno);
    }

    private boolean annoHasPermission(AnnotationMirror anno) {
        if (IFlowUtils.isPolySink(anno)) {
            return false; // Treat PolySink as top
        }
        Set<PFPermission> sinks = flowUtils.getSinks(anno);
        return sinks.contains(PFPermission.ANY) || sinks.contains(permission);
    }
}
