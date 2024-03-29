package checkers.inference;

import checkers.inference.solver.MaxSat2TypeSolver;
import checkers.inference.test.CFInferenceTest;
import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.org.plumelib.util.IPair;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NninfTest extends CFInferenceTest {

    public NninfTest(File testFile) {
        super(testFile, nninf.NninfChecker.class, "nninf",
                "-Anomsgtext", "-d", "tests/build/outputdir");
    }

    @Override
    public IPair<String, List<String>> getSolverNameAndOptions() {
        return IPair.of(MaxSat2TypeSolver.class.getCanonicalName(), new ArrayList<>());
    }

    @Parameters
    public static List<File> getTestFiles() {
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "nninf"));
        return testfiles;
    }
}
