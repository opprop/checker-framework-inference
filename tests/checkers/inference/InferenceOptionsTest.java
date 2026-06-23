package checkers.inference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import checkers.inference.InferenceLauncher.Mode;

public class InferenceOptionsTest {

    @Before
    public void resetOptions() {
        InferenceOptions.mode = null;
        InferenceOptions.hacks = false;
        InferenceOptions.typesystem = null;
        InferenceOptions.jaifFile = InferenceOptions.DEFAULT_JAIF;
        InferenceOptions.checker = null;
        InferenceOptions.solver = null;
        InferenceOptions.targetclasspath = ".";
        InferenceOptions.solverArgs = null;
        InferenceOptions.cfArgs = null;
        InferenceOptions.jsonFile = null;
        InferenceOptions.pathToAfuScripts = null;
        InferenceOptions.afuOutputDir = null;
        InferenceOptions.inPlace = false;
        InferenceOptions.afuOptions = null;
        InferenceOptions.makeDefaultsExplicit = false;
        InferenceOptions.version = false;
        InferenceOptions.help = false;
        InferenceOptions.logLevel = null;
        InferenceOptions.printCommands = false;
        InferenceOptions.debug = null;
        InferenceOptions.javacOptions = new ArrayList<String>();
        InferenceOptions.javaFiles = new String[0];
    }

    @Test
    public void parsesUppercaseMode() {
        InferenceOptions.InitStatus status =
                InferenceOptions.init(
                        new String[] {
                            "--mode=TYPECHECK", "--checker", ostrusted.OsTrustedChecker.class.getName()
                        },
                        true);

        assertTrue(status.errors.toString(), status.errors.isEmpty());
        assertEquals(Mode.TYPECHECK, InferenceOptions.mode);
    }

    @Test
    public void parsesLowercaseMode() {
        InferenceOptions.InitStatus status =
                InferenceOptions.init(
                        new String[] {
                            "--mode=typecheck", "--checker", ostrusted.OsTrustedChecker.class.getName()
                        },
                        true);

        assertTrue(status.errors.toString(), status.errors.isEmpty());
        assertEquals(Mode.TYPECHECK, InferenceOptions.mode);
    }

    @Test
    public void parsesHyphenatedMode() {
        InferenceOptions.InitStatus status =
                InferenceOptions.init(
                        new String[] {
                            "--mode=roundtrip-typecheck",
                            "--checker",
                            ostrusted.OsTrustedChecker.class.getName(),
                            "--jsonFile",
                            "constraints.json",
                            "--inPlace"
                        },
                        true);

        assertTrue(status.errors.toString(), status.errors.isEmpty());
        assertEquals(Mode.ROUNDTRIP_TYPECHECK, InferenceOptions.mode);
    }
}
