package checkers.inference;

import checkers.inference.solver.SolverEngine;
import checkers.inference.test.CFInferenceUnsatTest;
import org.checkerframework.framework.test.TestUtilities;
import org.checkerframework.javacutil.Pair;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OsTrustedUnsatTest extends CFInferenceUnsatTest {

    public OsTrustedUnsatTest(File testFile) {
        super(testFile,  ostrusted.OsTrustedChecker.class, "ostrusted",
                "-Anomsgtext",  "-Astubs=src/ostrusted/jdk.astub", "-d", "tests/build/outputdir");
    }

    @Override
    public Pair<String, List<String>> getSolverNameAndOptions() {
        return Pair.<String, List<String>>of(SolverEngine.class.getCanonicalName(), new ArrayList<String>());
    }

    @Parameterized.Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();//InferenceTestUtilities.findAllSystemTests();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testdata", "ostrusted-unsat-test"));
        return testfiles;
    }
}
