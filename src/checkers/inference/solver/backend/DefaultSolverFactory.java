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

public class DefaultSolverFactory implements SolverFactory {

    private final String BACKEND_PACKAGE_PATH = DefaultSolverFactory.class.getPackage().getName();

    private SolverConfiguration getSolverConfiguration(String solverType) {
        final String solverPackageName = BACKEND_PACKAGE_PATH + "." + solverType;
        final String solverConfigClassName = solverType.substring(0, 1).toUpperCase() + solverType.substring(1) + "SolverConfiguration";

        try {
            Class<?> solverConfigurationClass = Class.forName(solverPackageName + "." + solverConfigClassName);
            return (SolverConfiguration) solverConfigurationClass.getConstructor().newInstance();
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exceptions happends when getting the solver configuration for " + solverType, e);
            return null;
        }
        
    }

    @Override
    public SolverAdapter<?> createSolverAdapter(String solverType, Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        SolverConfiguration solverConfiguration = getSolverConfiguration(solverType);

        try {
            Constructor<?> solverAdpaterCons = solverConfiguration.getSolverAdapterClass().getConstructor(Map.class, Collection.class,
                    Collection.class, ProcessingEnvironment.class, solverConfiguration.getFormatTranslatorClass(), Lattice.class);

            return (SolverAdapter<?>) solverAdpaterCons.newInstance(configuration, slots, constraints,
                    processingEnvironment, formatTranslator, lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exception happends when creating solver adapter for " + solverType, e);
            return null;
        }
    }

    @Override
    public FormatTranslator<?, ?, ?> createFormatTranslator(String solverType, Lattice lattice) {
        final SolverConfiguration solverConfiguration = getSolverConfiguration(solverType);
        final Class<?> formatTranslatorClass = solverConfiguration.getFormatTranslatorClass();

        if (Modifier.isAbstract(formatTranslatorClass.getModifiers())) {
            ErrorReporter.errorAbort("Error: " + solverType + " doesn't have default format translator, type systems must provide their own implementation!");
        }

        try {
            Constructor<?> formatTranslatorCons = formatTranslatorClass.getConstructor(Lattice.class);
            return (FormatTranslator<?, ?, ?>) formatTranslatorCons.newInstance(lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort("Exception happends when creating format translator for " + solverType, e);
            return null;
        }
    }
}
