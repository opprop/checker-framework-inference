package checkers.inference.solver.backend.javasmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import java.math.BigInteger;

/** Smoke tests for the JavaSMT concrete solver runtimes used by the JavaSMT backend. */
public class JavaSmtSolverContextTest {

    @Test
    public void z3SupportsBitVectorModelGeneration() throws Exception {
        assertEquals(BigInteger.ONE, solveBitVectorFormula(Solvers.Z3));
    }

    @Test
    public void princessBitVectorSmokeTestDocumentsCurrentRuntimeGap() {
        assertBitVectorSmokeTestFails(Solvers.PRINCESS);
    }

    @Test
    public void smtInterpolBitVectorSmokeTestDocumentsCurrentRuntimeGap() {
        assertBitVectorSmokeTestFails(Solvers.SMTINTERPOL);
    }

    private static BigInteger solveBitVectorFormula(Solvers solver) throws Exception {
        try (SolverContext context = JavaSmtSolverContexts.create(solver);
                ProverEnvironment prover =
                        context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
            FormulaManager formulaManager = context.getFormulaManager();
            BitvectorFormulaManager bitvectorFormulaManager =
                    formulaManager.getBitvectorFormulaManager();
            BooleanFormulaManager booleanFormulaManager = formulaManager.getBooleanFormulaManager();
            BitvectorFormula variable = bitvectorFormulaManager.makeVariable(4, "x");
            BitvectorFormula one = bitvectorFormulaManager.makeBitvector(4, BigInteger.ONE);

            prover.addConstraint(
                    booleanFormulaManager.equivalence(
                            bitvectorFormulaManager.equal(variable, one),
                            booleanFormulaManager.makeTrue()));
            assertFalse(prover.isUnsat());

            try (Model model = prover.getModel()) {
                return model.evaluate(variable);
            }
        }
    }

    private static void assertBitVectorSmokeTestFails(Solvers solver) {
        Exception exception = assertThrows(Exception.class, () -> solveBitVectorFormula(solver));
        assertTrue(
                "Expected an explanatory failure for " + solver + ", got: " + exception,
                exception.getMessage() != null && !exception.getMessage().isEmpty());
    }
}
