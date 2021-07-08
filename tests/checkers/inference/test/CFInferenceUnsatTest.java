package checkers.inference.test;

import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.SystemUtil;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import java.io.File;
import java.util.List;

public abstract class CFInferenceUnsatTest extends CFInferenceTest{
    public CFInferenceUnsatTest(File testFile, Class<? extends AbstractProcessor> checker,
                           String testDir, String... checkerOptions) {
        super(testFile, checker, testDir, checkerOptions);
    }

    @Override
    @Test
    public void run() {
        boolean shouldEmitDebugInfo = TestUtilities.getShouldEmitDebugInfo();
        Pair<String, List<String>> solverArgs = getSolverNameAndOptions();

        final File testDataDir = new File("testdata");

        InferenceTestConfiguration config = InferenceTestConfigurationBuilder.buildDefaultConfiguration(testDir,
                testFile, testDataDir, checkerName, checkerOptions, getAdditionalInferenceOptions(), solverArgs.first,
                solverArgs.second, useHacks(), shouldEmitDebugInfo, getPathToAfuScripts(), getPathToInferenceScript());

        InferenceTestResult testResult = new InferenceTestExecutor().runTest(config);
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
