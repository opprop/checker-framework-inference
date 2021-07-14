package checkers.inference.test;

import org.checkerframework.javacutil.SystemUtil;

import javax.annotation.processing.AbstractProcessor;
import java.io.File;

/**
 * This test suite runs inference individually on target files that are unsatisfiable. A test case passes
 * when it passes the initial check phase (i.e. outputs expected errors/warnings and none unexpected ones),
 * while fails at the inference phase and then terminates.
 *
 * The use of this class is the same as {@link CFInferenceTest}. Note that the target directory of the test
 * cases can only contain unsatisfiable cases.
 */
public abstract class CFInferenceUnsatTest extends CFInferenceTest{
    public CFInferenceUnsatTest(File testFile, Class<? extends AbstractProcessor> checker,
                           String testDir, String... checkerOptions) {
        super(testFile, checker, testDir, checkerOptions);
    }

    @Override
    protected void postProcessResult(InferenceTestResult testResult) {
        final InferenceTestPhase lastPhaseRun = testResult.getLastPhaseRun();
        if (lastPhaseRun == InferenceTestPhase.INITIAL_TYPECHECK) {
            InferenceTestUtilities.assertResultsAreValid(testResult);

        } else if (lastPhaseRun == InferenceTestPhase.FINAL_TYPECHECK) {
            String summary = "Inference is expected to fail, but succeeded on source files: \n"
                    + SystemUtil.join("\n", testResult.getConfiguration().getInitialTypecheckConfig().getTestSourceFiles()) + "\n\n";
            InferenceTestUtilities.assertFail(InferenceTestPhase.INFER, summary);
        }
    }
}
