package checkers.inference.solver.backend;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.frontend.Lattice;

/**
 * SolverAdapter adapts a concrete underlying solver. This class is the super class
 * for all concrete solver adapters. For each concrete solver adapter, it adapts the
 * type constraint solving process to the underlying solver.
 *
 * A solver adapter takes type constraints from {@link checkers.inference.solver.SolverEngine}},
 * then delegates solving constraints responsibility to the underlying solver, and transform
 * underlying solver solution as a map between an integer and an annotation mirror as
 * the inferred result.
 *
 * Method {@link #solve()} is responsible for coordinating
 * above steps.
 * 
 * {@link #solve()} method is the entry point of the solver adapter, and it is got
 * called in class {@link checkers.inference.solver.SolverEngine}}. See
 * {@link checkers.inference.solver.SolverEngine#solveInparall()} and
 * {@link checkers.inference.solver.SolverEngine#solveInSequential()}.
 * 
 * @author jianchu
 *
 * @param <S> encoding type for slot
 * @param <T> encdoing type for constraint
 * @param <A> type for underlying solver's solution of a Slot
 */
public abstract class SolverAdapter<S, T, A> {

    /**
     * String key value pairs to configure the solver
     */
    protected final Map<String, String> configuration;

    /**
     * Collection of all slots will be used by underlying solver
     */
    protected final Collection<Slot> slots;

    /**
     * Collection of all constraints will be solved by underlying solver
     */
    protected final Collection<Constraint> constraints;

    /**
     * Target QualifierHierarchy
     */
    protected final QualifierHierarchy qualHierarchy;

    protected final ProcessingEnvironment processingEnvironment;

    /**
     * translator for encoding inference slots and constraints to underlying solver's constraints,
     * and decoding underlying solver's solution back to AnnotationMirrors.
     */
    protected final FormatTranslator<S, T, A> formatTranslator;

    /**
     * Set of ids of all variable solts will be used by underlying solver
     */
    protected final Set<Integer> varSlotIds;

    /**
     * Target qualifier lattice
     */
    protected final Lattice lattice;

    public SolverAdapter(Map<String, String> configuration, Collection<Slot> slots,
            Collection<Constraint> constraints, QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment, FormatTranslator<S, T, A> realTranslator, Lattice lattice) {
        this.configuration = configuration;
        this.slots = slots;
        this.constraints = constraints;
        this.qualHierarchy = qualHierarchy;
        this.processingEnvironment = processingEnvironment;
        this.formatTranslator = realTranslator;
        this.varSlotIds = new HashSet<Integer>();
        this.lattice = lattice;
    }

    /**
     * A concrete solver adapter needs to override this method and implements its own
     * constraint-solving strategy. In general, there will be three steps in this method:
     * 1. Calls {@link #convertAll()}, let {@link FormatTranslator} to convert constraints into
     * the corresponding encoding form.
     * 2. Calls the underlying solver to solve the encoding.
     * 3. Let {@link FormatTranslator} decodes the solution from the underlying solver and create a map between an 
     * Integer(Slot Id) and an AnnotationMirror as it's inferred annotation. 
     * 
     * It is the concrete solver adapter's responsibility to implemented the logic of above instructions and statistic collection. 
     * See {@link checkers.inference.solver.backend.maxsat.MaxSatSolver#solve()}} for an example.
     */
    public abstract Map<Integer, AnnotationMirror> solve();

    /**
     * Calls serializer to convert constraints into the corresponding encoding
     * form. See {@link checkers.inference.solver.backend.maxsat.MaxSatSolver#convertAll()}} for an example.
     */
    protected abstract void convertAll();

    /**
     * Get slot id from variable slot.
     *
     * @param constraint
     */
    protected void collectVarSlots(Constraint constraint) {
        for (Slot slot : constraint.getSlots()) {
            if (!(slot instanceof ConstantSlot)) {
                this.varSlotIds.add(((VariableSlot) slot).getId());
            }
        }
    }
}
