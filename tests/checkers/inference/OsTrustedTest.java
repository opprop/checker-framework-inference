package checkers.inference;

import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;
import org.plumelib.util.IPair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import checkers.inference.solver.MaxSat2TypeSolver;
import checkers.inference.test.CFInferenceTest;

public class OsTrustedTest extends CFInferenceTest {

    public OsTrustedTest(File testFile) {
        super(
                testFile,
                ostrusted.OsTrustedChecker.class,
                "ostrusted",
                "-Anomsgtext",
                "-Astubs=src/ostrusted/jdk.astub",
                "-d",
                "tests/build/outputdir");
    }

    @Override
    public IPair<String, List<String>> getSolverNameAndOptions() {
        return IPair.<String, List<String>>of(
                MaxSat2TypeSolver.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameters
    public static List<File> getTestFiles() {
        List<File> testfiles = new ArrayList<>(); // InferenceTestUtilities.findAllSystemTests();
        testfiles.addAll(
                TestUtilities.findRelativeNestedJavaFiles("testdata", "ostrusted-inferrable-test"));
        return testfiles;
    }
}
