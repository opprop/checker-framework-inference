package checkers.inference.solver.backend;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;

public class DefaultSolverFactory implements SolverFactory {

    private final String BACKEND_PACKAGE_PATH = DefaultSolverFactory.class.getPackage().getName();

    private SolverAdapterInfo getSolverAdapterInfo(String solverName) {
        final String solverPackageName = BACKEND_PACKAGE_PATH + "." + solverName;
        final String solverAdapterInfoClassName = solverName.substring(0, 1).toUpperCase() + solverName.substring(1) + "SolverInfo";

        try {
            Class<?> SolverAdapterInfoClass = Class.forName(solverPackageName + "." + solverAdapterInfoClassName);
            return (SolverAdapterInfo) SolverAdapterInfoClass.getConstructor().newInstance();
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when getting the solver configuration for " + solverName, e);
            return null;
        }
        
    }

    @Override
    public SolverAdapter<?> createSolverAdapter(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver.name());
        // Set default solver to maxsat, if a null solverName passed in.
        solverName = solverName == null ? "maxsat" : solverName;
        SolverAdapterInfo solverAdapterInfo = getSolverAdapterInfo(solverName);

        try {
            Constructor<?> solverAdpaterCons = solverAdapterInfo.getSolverAdapterClass().getConstructor(SolverOptions.class, Collection.class,
                    Collection.class, ProcessingEnvironment.class, solverAdapterInfo.getFormatTranslatorClass(), Lattice.class);

            return (SolverAdapter<?>) solverAdpaterCons.newInstance(solverOptions, slots, constraints,
                    processingEnvironment, formatTranslator, lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exception happends when creating solver adapter for " + solverName, e);
            return null;
        }
    }

    @Override
    public FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver.name());
        // Set default solver to maxsat, if a null solverName passed in.
        solverName = solverName == null ? "maxsat" : solverName;

        final SolverAdapterInfo solverAdapterInfo = getSolverAdapterInfo(solverName);
        final Class<?> formatTranslatorClass = solverAdapterInfo.getFormatTranslatorClass();

        if (Modifier.isAbstract(formatTranslatorClass.getModifiers())) {
            ErrorReporter.errorAbort("Error: " + solverName + " doesn't have default format translator, type systems must provide their own implementation!");
        }

        try {
            Constructor<?> formatTranslatorCons = formatTranslatorClass.getConstructor(Lattice.class);
            return (FormatTranslator<?, ?, ?>) formatTranslatorCons.newInstance(lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exception happends when creating format translator for " + solverName, e);
            return null;
        }
    }
}
