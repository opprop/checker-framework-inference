package checkers.inference.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.source.SourceChecker;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.VisitorState;
import org.checkerframework.javacutil.ErrorReporter;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.VariableAnnotator;

/**
 * Constraint manager holds constraints that are generated by InferenceVisitor.
 *
 * @author mcarthur
 *
 */
public class ConstraintManager {

    private boolean ignoreConstraints = false;

    private final Set<Constraint> constraints = new HashSet<Constraint>();

    private InferenceAnnotatedTypeFactory inferenceTypeFactory;

    private SourceChecker checker;

    private QualifierHierarchy realQualHierarchy;

    private VisitorState visitorState;

    public void init(InferenceAnnotatedTypeFactory inferenceTypeFactory) {
        this.inferenceTypeFactory = inferenceTypeFactory;
        this.realQualHierarchy = inferenceTypeFactory.getRealQualifierHierarchy();
        this.visitorState = inferenceTypeFactory.getVisitorState();
        this.checker = inferenceTypeFactory.getContext().getChecker();
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Adds the given constraint to the constraint set if the ignoreConstraints flag is set to
     * false, and the constraint is not an {@link AlwaysTrueConstraint}. An error is issued if the
     * constraint is an {@link AlwaysFalseConstraint}.
     */
    private void add(Constraint constraint) {
        if (!ignoreConstraints) {
            if (constraint instanceof AlwaysFalseConstraint) {
                ErrorReporter.errorAbort(
                        "Attempting to add an AlwaysFalseConstraint to the constraint set.");
            } else if (!(constraint instanceof AlwaysTrueConstraint)) {
                constraints.add(constraint);
            }
        }
    }

    public void startIgnoringConstraints() {
        ignoreConstraints = true;
    }

    public void stopIgnoringConstraints() {
        ignoreConstraints = false;
    }

    // All createXXXConstraint methods create a (possibly normalized) constraint for the given
    // slots. It does not issue errors for unsatisfiable constraints.

    /**
     * Creates a {@link SubtypeConstraint} between the two slots, which may be normalized to
     * {@link AlwaysTrueConstraint}, {@link AlwaysFalseConstraint}, or {@link EqualityConstraint}.
     */
    public Constraint createSubtypeConstraint(Slot subtype, Slot supertype) {
        return SubtypeConstraint.create(subtype, supertype, getCurrentLocation(),
                realQualHierarchy);
    }

    /**
     * Creates an {@link EqualityConstraint} between the two slots, which may be normalized to
     * {@link AlwaysTrueConstraint} or {@link AlwaysFalseConstraint}.
     */
    public Constraint createEqualityConstraint(Slot first, Slot second) {
        return EqualityConstraint.create(first, second, getCurrentLocation());
    }

    /**
     * Creates an {@link InequalityConstraint} between the two slots, which may be normalized to
     * {@link AlwaysTrueConstraint} or {@link AlwaysFalseConstraint}.
     */
    public Constraint createInequalityConstraint(Slot first, Slot second) {
        return InequalityConstraint.create(first, second, getCurrentLocation());
    }

    /**
     * Creates a {@link ComparableConstraint} between the two slots, which may be normalized to
     * {@link AlwaysTrueConstraint} or {@link AlwaysFalseConstraint}.
     */
    public Constraint createComparableConstraint(Slot first, Slot second) {
        return ComparableConstraint.create(first, second, getCurrentLocation(), realQualHierarchy);
    }

    /**
     * Creates a {@link CombineConstraint} between the three slots.
     */
    public CombineConstraint createCombineConstraint(Slot target, Slot decl, Slot result) {
        return CombineConstraint.create(target, decl, result, getCurrentLocation());
    }

    /**
     * Creates a {@link PreferenceConstraint} for the given slots with the given weight.
     */
    public PreferenceConstraint createPreferenceConstraint(VariableSlot variable, ConstantSlot goal,
            int weight) {
        return PreferenceConstraint.create(variable, goal, weight, getCurrentLocation());
    }

    /**
     * Creates an {@link ExistentialConstraint} for the given slot and lists of constraints.
     */
    public ExistentialConstraint createExistentialConstraint(Slot slot,
            List<Constraint> ifExistsConstraints, List<Constraint> ifNotExistsConstraints) {
        return ExistentialConstraint.create((VariableSlot) slot, ifExistsConstraints,
                ifNotExistsConstraints, getCurrentLocation());
    }

    // TODO: give location directly in Constraint.create() methods
    private AnnotationLocation getCurrentLocation() {
        if (visitorState.getPath() != null) {
            return VariableAnnotator.treeToLocation(inferenceTypeFactory,
                    visitorState.getPath().getLeaf());
        } else {
            return AnnotationLocation.MISSING_LOCATION;
        }
    }

    // All addXXXConstraint methods create a (possibly normalized) constraint for the given slots
    // and issues errors for unsatisfiable constraints.

    /**
     * Checks that the given constraint is not an instance of {@link AlwaysFalseConstraint}, and
     * then adds it to the set of constraints. If the constraint is an instance of
     * {@link AlwaysFalseConstraint}, an error message is issued.
     */
    private void validateAndAddConstraint(Class<? extends Constraint> sourceConstraintClass,
            Constraint normalizedConstraint) {
        if (normalizedConstraint instanceof AlwaysFalseConstraint) {
            // converts "XxxConstraint" to "xxx.constraint.unsatisfiable" as an error key
            String errorKey = sourceConstraintClass.getSimpleName().toLowerCase()
                    .replace("constraint", ".constraint.unsatisfiable");

            // issue a non-halting error message indicating which constraint cannot be satisfied
            checker.report(Result.failure(errorKey), visitorState.getPath().getLeaf());
        } else {
            add(normalizedConstraint);
        }
    }

    /**
     * Creates and adds a {@link SubtypeConstraint} between the two slots to the constraint set,
     * which may be normalized to {@link AlwaysTrueConstraint} or {@link EqualityConstraint}. An
     * error is issued if the {@link SubtypeConstraint} is always unsatisfiable.
     */
    public void addSubtypeConstraint(Slot subtype, Slot supertype) {
        validateAndAddConstraint(SubtypeConstraint.class,
                createSubtypeConstraint(subtype, supertype));
    }

    /**
     * Creates and adds an {@link EqualityConstraint} between the two slots to the constraint set,
     * which may be normalized to {@link AlwaysTrueConstraint}. An error is issued if the
     * {@link EqualityConstraint} is always unsatisfiable.
     */
    public void addEqualityConstraint(Slot first, Slot second) {
        validateAndAddConstraint(EqualityConstraint.class, createEqualityConstraint(first, second));
    }

    /**
     * Creates and adds an {@link InequalityConstraint} between the two slots to the constraint set,
     * which may be normalized to {@link AlwaysTrueConstraint}. An error is issued if the
     * {@link InequalityConstraint} is always unsatisfiable.
     */
    public void addInequalityConstraint(Slot first, Slot second) {
        validateAndAddConstraint(InequalityConstraint.class,
                createInequalityConstraint(first, second));
    }

    /**
     * Creates and adds a {@link ComparableConstraint} between the two slots to the constraint set,
     * which may be normalized to {@link AlwaysTrueConstraint}. An error is issued if the
     * {@link ComparableConstraint} is always unsatisfiable.
     */
    public void addComparableConstraint(Slot first, Slot second) {
        validateAndAddConstraint(ComparableConstraint.class,
                createComparableConstraint(first, second));
    }

    /**
     * Creates and adds a {@link CombineConstraint} to the constraint set. An error is issued if the
     * {@link CombineConstraint} is always unsatisfiable.
     */
    public void addCombineConstraint(Slot target, Slot decl, Slot result) {
        validateAndAddConstraint(CombineConstraint.class,
                createCombineConstraint(target, decl, result));
    }

    /**
     * Creates and adds a {@link PreferenceConstraint} to the constraint set. An error is issued if
     * the {@link PreferenceConstraint} is always unsatisfiable.
     */
    public void addPreferenceConstraint(VariableSlot variable, ConstantSlot goal, int weight) {
        validateAndAddConstraint(PreferenceConstraint.class,
                createPreferenceConstraint(variable, goal, weight));
    }

    /**
     * Creates and adds a {@link ExistentialConstraint} to the constraint set. An error is issued if
     * the {@link ExistentialConstraint} is always unsatisfiable.
     */
    public void addExistentialConstraint(Slot slot, List<Constraint> ifExistsConstraints,
            List<Constraint> ifNotExistsConstraints) {
        validateAndAddConstraint(ExistentialConstraint.class,
                createExistentialConstraint(slot, ifExistsConstraints, ifNotExistsConstraints));
    }
}
