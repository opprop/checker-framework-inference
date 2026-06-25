package checkers.inference.solver.backend.javasmt;

import com.microsoft.z3.Context;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.SolverContext;

/** Creates JavaSMT solver contexts with this project's solver-runtime setup. */
final class JavaSmtSolverContexts {

    private JavaSmtSolverContexts() {}

    static SolverContext create(Solvers javaSmtSolver) throws Exception {
        if (javaSmtSolver == Solvers.Z3) {
            preloadZ3();
            return SolverContextFactory.createSolverContext(
                    Configuration.defaultConfiguration(),
                    LogManager.createNullLogManager(),
                    ShutdownNotifier.createDummy(),
                    javaSmtSolver,
                    ignored -> {});
        }
        return SolverContextFactory.createSolverContext(javaSmtSolver);
    }

    private static void preloadZ3() {
        try (Context ignored = new Context()) {
            // z3-turnkey extracts and loads the bundled native libraries here.
        }
    }
}
