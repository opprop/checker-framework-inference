package checkers.inference.solver.backend;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.solver.util.SolverOptions;

public class DefaultSolverFactory implements SolverFactory {

    private final String BACKEND_PACKAGE_PATH = DefaultSolverFactory.class.getPackage().getName();

    private SolverConfiguration getSolverConfiguration(String solverName) {
        // Set default solver to maxsat, if a null solverName passed in.
        solverName = solverName == null ? "maxsat" : solverName;

        final String solverPackageName = BACKEND_PACKAGE_PATH + "." + solverName;
        final String solverConfigClassName = solverName.substring(0, 1).toUpperCase() + solverName.substring(1) + "SolverConfiguration";

        try {
            Class<?> solverConfigurationClass = Class.forName(solverPackageName + "." + solverConfigClassName);
            return (SolverConfiguration) solverConfigurationClass.getConstructor().newInstance();
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when getting the solver configuration for " + solverName, e);
            return null;
        }
        
    }

    @Override
    public final SolverAdapter<?> createSolverAdapter(SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver.name());
        return createSolverAdapter(solverName, solverOptions, slots, constraints, processingEnvironment, lattice, formatTranslator);
    }

    @Override
    public final FormatTranslator<?, ?, ?> createFormatTranslator(SolverOptions solverOptions, Lattice lattice) {
        String solverName = solverOptions.getArg(SolverFactoryArg.solver.name());
        return createFormatTranslator(solverName, solverOptions, lattice);
    }

    protected SolverAdapter<?> createSolverAdapter(String solverName, SolverOptions solverOptions,
            Collection<Slot> slots, Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        SolverConfiguration solverConfiguration = getSolverConfiguration(solverName);

        try {
            Constructor<?> solverAdpaterCons = solverConfiguration.getSolverAdapterClass().getConstructor(Map.class, Collection.class,
                    Collection.class, ProcessingEnvironment.class, solverConfiguration.getFormatTranslatorClass(), Lattice.class);

            return (SolverAdapter<?>) solverAdpaterCons.newInstance(solverOptions, slots, constraints,
                    processingEnvironment, formatTranslator, lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exception happends when creating solver adapter for " + solverName, e);
            return null;
        }
    }

    protected FormatTranslator<?, ?, ?> createFormatTranslator(String solverName, SolverOptions solverOptions, Lattice lattice) {
        final SolverConfiguration solverConfiguration = getSolverConfiguration(solverName);
        final Class<?> formatTranslatorClass = solverConfiguration.getFormatTranslatorClass();

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
