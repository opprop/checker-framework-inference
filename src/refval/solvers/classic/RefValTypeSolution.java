package refval.solvers.classic;

import java.util.HashMap;
import java.util.Map;

public class RefValTypeSolution {
    private final Map<Integer, Boolean> result;
    private final String refValType;
    private final boolean isRoot;

    public RefValTypeSolution(Map<Integer, Boolean> result, String refValType, boolean isRoot) {
        this.result = result;
        this.refValType = refValType;
        this.isRoot = isRoot;
    }

    private RefValTypeSolution(String refValType, boolean isRoot) {
        this(new HashMap<Integer, Boolean>(), refValType, isRoot);
    }

    public Map<Integer, Boolean> getResult() {
        return result;
    }

    public String getRefValType() {
        return refValType;
    }

    public static RefValTypeSolution noSolution(String datatype) {
        return new RefValTypeSolution(datatype, false);
    }

    public boolean isRoot() {
        return this.isRoot;
    }

}
