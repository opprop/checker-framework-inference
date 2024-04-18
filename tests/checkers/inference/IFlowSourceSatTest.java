package checkers.inference;

import checkers.inference.test.CFInferenceTest;
import org.checkerframework.framework.test.TestUtilities;
import org.plumelib.util.IPair;
import org.junit.runners.Parameterized.Parameters;
import sparta.checkers.IFlowSourceChecker;
import sparta.checkers.sat.SourceSolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IFlowSourceSatTest extends CFInferenceTest {

    public IFlowSourceSatTest(File testFile) {
        super(testFile,  IFlowSourceChecker.class, "sparta"+File.separator+"checkers",
                "-Anomsgtext",  "-Astubs=src/sparta/checkers/information_flow.astub", "-d", "tests/build/outputdir");
    }

    @Override
    public IPair<String, List<String>> getSolverNameAndOptions() {
        return IPair.<String, List<String>>of(SourceSolver.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();//InferenceTestUtilities.findAllSystemTests();
        if (isAtMost7Jvm) {
            testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "iflowsource"));
        }
        return testfiles;
    }
}
