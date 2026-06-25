package sparta.checkers.sat;

import checkers.inference.InferenceMain;
import checkers.inference.model.serialization.CnfVecIntSerializer;
import sparta.checkers.iflow.util.PFPermission;

public abstract class IFlowSerializer extends CnfVecIntSerializer {
    protected PFPermission permission;

    public IFlowSerializer(PFPermission permission) {
        super(InferenceMain.getInstance().getSlotManager());
        this.permission = permission;
    }
}
