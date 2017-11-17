package checkers.inference.solver.backend;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.maxsat.MaxSatSolver;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.NameUtils;
import checkers.inference.solver.util.SolverOptions;
import checkers.inference.util.ConstraintVerifier;

public class DefaultSolverFactory implements SolverFactory {

    private final String BACKEND_PACKAGE_PATH = DefaultSolverFactory.class.getPackage().getName();

    private SolverFactory getUnderlyingSolverFactory(String solverName) {
        final String solverPackageName = BACKEND_PACKAGE_PATH + "." + solverName.toLowerCase();
        final String solverFactoryClassName = solverName + "SolverFactory";

        try {
            Class<?> SolverFactoryClass = Class.forName(solverPackageName + "." + solverFactoryClassName);
            return (SolverFactory) SolverFactoryClass.getConstructor().newInstance();
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when getting the solver factory for " + solverName, e);
            return null;
        }
    }

    @Override
    public Solver<?> createSolver(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver);
        // Set default solver to maxsat, if a null solverName passed in.
        solverName = solverName == null ? NameUtils.getSolverName(MaxSatSolver.class) : solverName;
        SolverFactory underlyingSolverFactory = getUnderlyingSolverFactory(solverName);
        return underlyingSolverFactory.createSolver(solverOptions, slots, constraints, processingEnvironment, lattice, formatTranslator);
    }

    @Override
    public FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice,
            ConstraintVerifier verifier) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver);
        // Set default solver to maxsat, if a null solverName passed in.
        solverName = solverName == null ? NameUtils.getSolverName(MaxSatSolver.class) : solverName;

        SolverFactory underlyingSolverFactory = getUnderlyingSolverFactory(solverName);
        return underlyingSolverFactory.createFormatTranslator(solverOptions, lattice, verifier);
    }

}
