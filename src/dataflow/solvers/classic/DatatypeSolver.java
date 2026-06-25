package dataflow.solvers.classic;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.Constraint;

public class DatatypeSolver {
    private final SlotManager slotManager;
    private final String datatype;
    private final DataflowSerializer serializer;
    private final List<VecInt> clauses;

    public DatatypeSolver(
            String datatype, Collection<Constraint> constraints, DataflowSerializer serializer) {
        this.datatype = datatype;
        this.serializer = serializer;
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.clauses = convertToCNF(constraints);
    }

    private List<VecInt> convertToCNF(Collection<Constraint> constraints) {
        return serializer.convertAll(constraints);
    }

    public DatatypeSolution solve() {
        Map<Integer, Boolean> result = new HashMap<>();

        final int totalVars = slotManager.getNumberOfSlots();
        final int totalClauses = clauses.size();

        try {
            // **** Prep Solver ****
            // org.sat4j.pb.SolverFactory.newBoth() Runs both of sat4j solves and uses the result of
            // the first to finish
            // JLTODO: why is this a weighted max-sat solver? Isn't this only
            // creating sat constraints?
            final WeightedMaxSatDecorator solver =
                    new WeightedMaxSatDecorator(org.sat4j.pb.SolverFactory.newBoth());

            solver.newVar(totalVars);
            solver.setExpectedNumberOfClauses(totalClauses);
            // Arbitrary timeout
            solver.setTimeoutMs(1000000);
            for (VecInt clause : clauses) {
                solver.addSoftClause(clause);
            }

            // **** Solve ****
            boolean hasSolution = solver.isSatisfiable();

            if (hasSolution) {

                // **** Remove exatential vars from solution
                final Map<Integer, Integer> existentialToPotentialIds =
                        serializer.getExistentialToPotentialVar();
                int[] solution = solver.model();
                for (Integer var : solution) {
                    boolean varIsTrue = var > 0;
                    // Need postive var
                    var = Math.abs(var);
                    Integer potential = existentialToPotentialIds.get(var);
                    if (potential == null) {
                        // Logic is same as sparta.SourceResult, but for easy
                        // to understand, I just set True for each top, which
                        // means this top(type) should present:
                        // If the solution is false, that means top was
                        // inferred.
                        // For dataflow, that means that the annotation should
                        // have the type.
                        result.put(var, !varIsTrue);
                    }
                }
                // System.out.println("*******************************");
                return new DatatypeSolution(result, datatype, this.serializer.isRoot());
            }

        } catch (Throwable th) {
            VecInt lastClause = clauses.get(clauses.size() - 1);
            throw new RuntimeException("Error MAX-SAT solving! " + lastClause, th);
        }

        return DatatypeSolution.noSolution(datatype);
    }
}
