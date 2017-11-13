package checkers.inference.solver.util;

import java.util.Collections;
import java.util.Map;

public class SolverOptions {

    /**
     * Map of configuration. Key is argument name, value is argument value.
     */
    private final Map<String, String> options;

    public SolverOptions(final Map<String, String> configuration) {
        this.options = Collections.unmodifiableMap(configuration);
    }

    /**
     * Get the value for a given argument name.
     * @param argName the name of the given argument.
     * @return the string value for a given argument name.
     */
    public String getArg(String argName) {
        return options.get(argName);
    }

    /**
     * Get the boolean value for a given argument name.
     *
     * @param argName the name of the given argument.
     * @return true if the lower case of the string value of this argument equals to "true",
     * otherwise return false.
     */
    public boolean getBoolArg(String argName) {
        String argValue = options.get(argName);
        return argValue != null && argValue.toLowerCase().equals("true");
    }
}
