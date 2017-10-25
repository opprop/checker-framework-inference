package checkers.inference.solver.backend.maxsat;

import checkers.inference.solver.frontend.Lattice;

/**
 * Methods convert between slot id and Max-SAT id.
 * 
 * @author jianchu
 *
 */
public class MathUtils {

    public static int mapIdToMatrixEntry(int varId, int typeInt, Lattice lattice) {
        int column = typeInt + 1;
        int row = varId - 1;
        int length = lattice.numTypes;
        return column + row * length;
    }

    public static int getSlotId(int var, Lattice lattice) {
        return (Math.abs(var) / lattice.numTypes + 1);
    }

    public static int getIntRep(int var, Lattice lattice) {
        return Math.abs(var) - (Math.abs(var) / lattice.numTypes) * lattice.numTypes;
    }
}
