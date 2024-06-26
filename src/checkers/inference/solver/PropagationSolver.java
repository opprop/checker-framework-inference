package checkers.inference.solver;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.DefaultInferenceResult;
import checkers.inference.InferenceMain;
import checkers.inference.InferenceResult;
import checkers.inference.InferenceSolver;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.Slot;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.VariableSlot;

/**
 * InferenceSolver FloodSolver implementation
 *
 * <p>TODO: Parameters to configure where to push conflicts?
 *
 * @author mcarthur
 */
public class PropagationSolver implements InferenceSolver {

    // private QualifierHierarchy qualHierarchy;
    private Collection<Constraint> constraints;
    private Collection<Slot> slots;

    private AnnotationMirror defaultValue;
    private AnnotationMirror top;
    private AnnotationMirror bottom;

    @Override
    public InferenceResult solve(
            Map<String, String> configuration,
            Collection<Slot> slots,
            Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        this.slots = slots;
        this.constraints = constraints;
        // this.qualHierarchy = qualHierarchy;

        this.top = qualHierarchy.getTopAnnotations().iterator().next();
        this.bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        // TODO: This needs to be parameterized based on the type system
        this.defaultValue = top;

        return solve();
    }

    /**
     * Flood solve a list of constraints.
     *
     * <p>1) Find all variables that must be top (@TOP <: Var or VAR == @TOP)
     *
     * <p>2) Find all variables that must be bot (Var <: @BOT or VAR == @BOT)
     *
     * <p>3) From constraints, create propagation maps. These maps one variable to a list of other
     * variables. If the key variable is a certain annotation the variables in the value list must
     * also be that annotation. A map is create for subtype propagation and supertype propagation.
     *
     * <p>As an example, given a subtype propagation map of: @1 -> [ @2, @3 ]
     *
     * <p>If @1 was inferred to be @BOT, then @2 and @3 would also have to be bot.
     *
     * <p>4) Propagate the supertype values first
     *
     * <p>5) Propagate the subtype values second
     *
     * <p>6) Merge the results to get just one AnnotationMirror for each variable.
     *
     * @return Map of int variable id to its inferred AnnotationMirror value
     */
    public InferenceResult solve() {

        Set<Slot> fixedBottom = new HashSet<Slot>();
        Set<Slot> fixedTop = new HashSet<Slot>();
        Map<Slot, List<Slot>> superTypePropagation = new HashMap<>();
        Map<Slot, List<Slot>> subTypePropagation = new HashMap<>();

        preprocessConstraints(fixedBottom, fixedTop, superTypePropagation, subTypePropagation);

        // Propagate supertype
        Set<Slot> inferredTop = propagateValues(fixedTop, superTypePropagation);

        // Propagate subtype
        Set<Slot> inferredBottom = propagateValues(fixedBottom, subTypePropagation);
        return mergeToResult(fixedBottom, fixedTop, inferredTop, inferredBottom);
    }

    /**
     * Perform steps 1-3 of flood solving.
     *
     * <p>The parameters are the results of processing.
     *
     * <p>fixedBottom and fixedTop contain relationships between variables and constants (the
     * constant for bottom and the constant for top respectively)
     *
     * <p>superTypePropagation and subTypePropagation
     *
     * @param fixedBottom Variables that must be bottom
     * @param fixedTop Variables that must be top
     * @param superTypePropagation Map, where if a key is a supertyp, all variables in the value
     *     must also be supertype
     * @param subTypePropagation Map, where if a key is a subtype, all variables in the value must
     *     also be subtypes
     */
    private void preprocessConstraints(
            Set<Slot> fixedBottom,
            Set<Slot> fixedTop,
            Map<Slot, List<Slot>> superTypePropagation,
            Map<Slot, List<Slot>> subTypePropagation) {

        for (Constraint constraint : constraints) {
            // Skip constraints that are just constants
            if (!checkContainsVariable(constraint)) {
                continue;
            }

            if (constraint instanceof EqualityConstraint) {
                EqualityConstraint equality = (EqualityConstraint) constraint;
                if (equality.getFirst() instanceof ConstantSlot) {
                    // Equal to a constant forces a constant
                    AnnotationMirror value = ((ConstantSlot) equality.getFirst()).getValue();
                    Slot variable = equality.getSecond();
                    if (AnnotationUtils.areSame(value, top)) {
                        fixedTop.add(variable);
                    } else {
                        fixedBottom.add(variable);
                    }
                } else if (equality.getSecond() instanceof ConstantSlot) {
                    // Equal to a constant forces a constant
                    AnnotationMirror value = ((ConstantSlot) equality.getSecond()).getValue();
                    Slot variable = equality.getFirst();
                    if (AnnotationUtils.areSame(value, top)) {
                        fixedTop.add(variable);
                    } else {
                        fixedBottom.add(variable);
                    }
                } else {
                    // Variable equality means values of one propagates to values of the other, for
                    // both subtype and supertype
                    addEntryToMap(
                            superTypePropagation,
                            equality.getFirst(),
                            equality.getSecond(),
                            constraint);
                    addEntryToMap(
                            superTypePropagation,
                            equality.getSecond(),
                            equality.getFirst(),
                            constraint);
                    addEntryToMap(
                            subTypePropagation,
                            equality.getFirst(),
                            equality.getSecond(),
                            constraint);
                    addEntryToMap(
                            subTypePropagation,
                            equality.getSecond(),
                            equality.getFirst(),
                            constraint);
                }
            } else if (constraint instanceof SubtypeConstraint) {
                SubtypeConstraint subtype = (SubtypeConstraint) constraint;
                if (subtype.getSubtype() instanceof ConstantSlot) {
                    // If top is a subtype of a variable, that variable is top
                    AnnotationMirror value = ((ConstantSlot) subtype.getSubtype()).getValue();
                    Slot variable = subtype.getSupertype();
                    if (AnnotationUtils.areSame(value, top)) {
                        fixedTop.add(variable);
                    }
                } else if (subtype.getSupertype() instanceof ConstantSlot) {
                    // If a variable is a subtype of bottom, that variable is bottom
                    AnnotationMirror value = ((ConstantSlot) subtype.getSupertype()).getValue();
                    Slot variable = subtype.getSubtype();
                    if (AnnotationUtils.areSame(value, bottom)) {
                        fixedBottom.add(variable);
                    }
                } else {
                    // If the RHS is top, the LHS must be top
                    addEntryToMap(
                            superTypePropagation,
                            subtype.getSubtype(),
                            subtype.getSupertype(),
                            constraint);
                    // If the LHS is bottom, the RHS must be bottom
                    addEntryToMap(
                            subTypePropagation,
                            subtype.getSupertype(),
                            subtype.getSubtype(),
                            constraint);
                }
            } else if (constraint instanceof ExistentialConstraint) {
                InferenceMain.getInstance()
                        .logger
                        .warning(
                                "PropagationSolver: Existential constraint found.  Inferred annotations may not type check ");
            }
        }
    }

    /**
     * Given the inferred values, return a value for each slot.
     *
     * <p>Variables will have conflicting values if the constraints were not solvable.
     *
     * <p>This currently gives value precedence to fixedBottom, fixedTop, inferredBottom,
     * inferredTop
     *
     * @return
     */
    private InferenceResult mergeToResult(
            Set<Slot> fixedBottom,
            Set<Slot> fixedTop,
            Set<Slot> inferredTop,
            Set<Slot> inferredBottom) {

        Map<Integer, AnnotationMirror> solutions = new HashMap<Integer, AnnotationMirror>();
        for (Slot slot : slots) {
            if (slot instanceof VariableSlot) {
                AnnotationMirror result;
                if (fixedBottom.contains(slot)) {
                    result = bottom;
                } else if (fixedTop.contains(slot)) {
                    result = top;
                } else if (inferredBottom.contains(slot)) {
                    result = bottom;
                } else if (inferredTop.contains(slot)) {
                    result = top;
                } else {
                    result = defaultValue;
                }
                if (result != defaultValue) {
                    solutions.put(slot.getId(), result);
                }
            }
        }

        return new DefaultInferenceResult(solutions);
    }

    /**
     * Given starting fixed values, iterate on the propagation map to propagate the resulting
     * values.
     *
     * @param fixed The starting values that will trigger propagation
     * @param typePropagation Maps of value to list of other values that will be propagated when the
     *     key is triggered.
     * @return All values that were fixed flooded/propagated to.
     */
    private Set<Slot> propagateValues(Set<Slot> fixed, Map<Slot, List<Slot>> typePropagation) {

        Set<Slot> results = new HashSet<Slot>();

        Set<Slot> worklist = new HashSet<Slot>(fixed);
        while (!worklist.isEmpty()) {
            Slot variable = worklist.iterator().next();
            worklist.remove(variable);
            if (typePropagation.containsKey(variable)) {
                List<Slot> inferred = typePropagation.get(variable);
                List<Slot> inferredVars = new ArrayList<Slot>();
                inferredVars.addAll(inferred);
                inferredVars.removeAll(results);
                results.addAll(inferredVars);
                worklist.addAll(inferredVars);
            }
        }
        return results;
    }

    private boolean checkContainsVariable(Constraint constraint) {
        boolean containsVariable = false;
        for (Slot slot : constraint.getSlots()) {
            if (slot instanceof VariableSlot) {
                containsVariable = true;
            }
        }
        return containsVariable;
    }

    void addEntryToMap(Map<Slot, List<Slot>> entries, Slot key, Slot value, Constraint constraint) {
        List<Slot> valueList;
        if (entries.get(key) == null) {
            valueList = new ArrayList<>();
            entries.put(key, valueList);
        } else {
            valueList = entries.get(key);
        }
        valueList.add(value);
    }
}
