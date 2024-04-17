package checkers.inference;

import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;
import org.plumelib.util.IPair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import checkers.inference.test.CFInferenceTest;
import sparta.checkers.IFlowSinkChecker;
import sparta.checkers.sat.SinkSolver;

public class IFlowSinkSatTest extends CFInferenceTest {

    public IFlowSinkSatTest(File testFile) {
        super(
                testFile,
                IFlowSinkChecker.class,
                "sparta" + File.separator + "checkers",
                "-Anomsgtext",
                "-Astubs=src/sparta/checkers/information_flow.astub",
                "-d",
                "tests/build/outputdir");
    }

    @Override
    public IPair<String, List<String>> getSolverNameAndOptions() {
        return IPair.<String, List<String>>of(
                SinkSolver.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameters
    public static List<File> getTestFiles() {
        List<File> testfiles = new ArrayList<>(); // InferenceTestUtilities.findAllSystemTests();
        if (isAtMost7Jvm) {
            testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "iflowsink"));
        }
        return testfiles;
    }
}
