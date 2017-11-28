package checkers.inference.solver.util;

import java.util.Collections;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

public class SolverEnvironment {

    /**
     * Map of configuration. Key is argument name, value is argument value.
     */
    private final Map<String, String> options;

    /**
     * Processing environment providing by the tool framework.
     */
    public final ProcessingEnvironment processingEnvironment;

    public SolverEnvironment(final Map<String, String> configuration, ProcessingEnvironment processingEnvironment) {
        this.options = Collections.unmodifiableMap(configuration);
        this.processingEnvironment = processingEnvironment;
    }

    /**
     * Get the value for a given argument name.
     * @param argName the name of the given argument.
     * @return the string value for a given argument name.
     */
    public String getArg(SolverArg arg) {
        return options.get(arg.name());
    }

    /**
     * Get the boolean value for a given argument name.
     *
     * @param argName the name of the given argument.
     * @return true if the lower case of the string value of this argument equals to "true",
     * otherwise return false.
     */
    public boolean getBoolArg(SolverArg arg) {
        String argValue = options.get(arg.name());
        return argValue != null && argValue.toLowerCase().equals("true");
    }
}
