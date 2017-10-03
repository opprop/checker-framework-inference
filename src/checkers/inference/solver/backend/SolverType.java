package checkers.inference.solver.backend;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.ErrorReporter;

import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.logiql.LogiQLFormatTranslator;
import checkers.inference.solver.backend.logiql.LogiQLSolver;
import checkers.inference.solver.backend.maxsat.LingelingSolver;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.MaxSatSolver;
import checkers.inference.solver.frontend.Lattice;

public enum SolverType {

    MAXSAT("MaxSAT", MaxSatSolver.class, MaxSatFormatTranslator.class),
    LINGELING("Lingeling", LingelingSolver.class, MaxSatFormatTranslator.class),
    LOGIQL("LogiQL", LogiQLSolver.class, LogiQLFormatTranslator.class);

    public final String simpleName;
    public final Class<? extends SolverAdapter<?>> solverAdapterClass;
    public final Class<? extends FormatTranslator<?, ?, ?>> translatorClass;

    private SolverType(String simpleName, Class<? extends SolverAdapter<?>> solverAdapterClass,
            Class<? extends FormatTranslator<?, ?, ?>> translatorClass) {
        this.simpleName = simpleName;
        this.solverAdapterClass = solverAdapterClass;
        this.translatorClass = translatorClass;
    }

    public FormatTranslator<?, ?, ?> createDefaultFormatTranslator(Lattice lattice) {
        Constructor<?> cons;
        try {
            cons = translatorClass.getConstructor(Lattice.class);
            return (FormatTranslator<?, ?, ?>) cons.newInstance(lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort(
                    "Exception happends when creating default serializer for " + simpleName + " backend.", e);
            // Dead code.
            return null;
        }
    }

    public SolverAdapter<?> createSolverAdapter(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy, ProcessingEnvironment processingEnvironment,
            Lattice lattice, FormatTranslator<?, ?, ?> formatTranslator) {
        try {
            Constructor<?> cons = solverAdapterClass.getConstructor(Map.class, Collection.class,
                    Collection.class, QualifierHierarchy.class, ProcessingEnvironment.class,
                    FormatTranslator.class, Lattice.class);

            return (SolverAdapter<?>) cons.newInstance(configuration, slots, constraints, qualHierarchy,
                    processingEnvironment, formatTranslator, lattice);
        } catch (Exception e) {
            ErrorReporter.errorAbort(
                    "Exception happends when creating " + simpleName + " backend.", e);
            // Dead code.
            return null;
        }
    }

    public static SolverType getSolverType(String simpleName) {
        for (SolverType solverType : SolverType.values()) {
            if (solverType.simpleName.equals(simpleName)) {
                return solverType;
            }
        }
        return null;
    }
}
