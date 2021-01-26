package checkers.typecheck;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import refval.RefValChecker;

public class RefValTypecheckTest extends CheckerFrameworkPerFileTest {

    public RefValTypecheckTest(File testFile) {
        super(testFile,  RefValChecker.class, "refval",
                "-Anomsgtext", "-d", "tests/build/outputdir");
    }

    @Parameters
    public static List<File> getTestFiles(){
        List<File> testfiles = new ArrayList<>();
        testfiles.addAll(TestUtilities.findRelativeNestedJavaFiles("testing", "refval-not-inferrable-test"));
        return testfiles;
    }
}
