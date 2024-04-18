package checkers.inference;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

public class BaseInferenceRealTypeFactory extends BaseAnnotatedTypeFactory {

    public BaseInferenceRealTypeFactory(BaseTypeChecker checker, boolean isInfer) {
        // Only use dataflow analysis when we're not doing inference
        super(checker, !isInfer);

        if (this.getClass() == BaseInferenceRealTypeFactory.class) {
            postInit();
        }
    }
}
