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
import checkers.inference.model.ArithmeticConstraint.ArithmeticOperationKind;
import checkers.inference.util.ConstraintVerifier;

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

    private ConstraintVerifier constraintVerifier;

    public void init(InferenceAnnotatedTypeFactory inferenceTypeFactory) {
        this.inferenceTypeFactory = inferenceTypeFactory;
        this.realQualHierarchy = inferenceTypeFactory.getRealQualifierHierarchy();
        this.visitorState = inferenceTypeFactory.getVisitorState();
        this.checker = inferenceTypeFactory.getContext().getChecker();
        this.constraintVerifier = new ConstraintVerifier(realQualHierarchy);
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public ConstraintVerifier getConstraintVerifier() {
        return constraintVerifier;
    }

    private void add(Constraint constraint) {
        if (!ignoreConstraints) {
            constraints.add(constraint);
        }
    }

    public void startIgnoringConstraints() {
        ignoreConstraints = true;
    }

    public void stopIgnoringConstraints() {
        ignoreConstraints = false;
    }

    public SubtypeConstraint createSubtypeConstraint(Slot subtype, Slot supertype) {
        if (subtype == null || supertype == null) {
            ErrorReporter.errorAbort("Create subtype constraint with null argument. Subtype: " + subtype
                    + " Supertype: " + supertype);
        }
        if (subtype instanceof ConstantSlot && supertype instanceof ConstantSlot) {
            ConstantSlot subConstant = (ConstantSlot) subtype;
            ConstantSlot superConstant = (ConstantSlot) supertype;

            if (!constraintVerifier.isSubtype(subConstant, superConstant)) {
                checker.report(Result.failure("subtype.constraint.unsatisfiable", subtype, supertype),
                        visitorState.getPath().getLeaf());
            }
        }
        return new SubtypeConstraint(subtype, supertype, getCurrentLocation());
    }

    public EqualityConstraint createEqualityConstraint(Slot lhs, Slot rhs) {
        if (lhs == null || rhs == null) {
            ErrorReporter.errorAbort("Create equality constraint with null argument. Subtype: "
                    + lhs + " Supertype: " + rhs);
        }
        if (lhs instanceof ConstantSlot && rhs instanceof ConstantSlot) {
            ConstantSlot lhsConstant = (ConstantSlot) lhs;
            ConstantSlot rhsConstant = (ConstantSlot) rhs;
            if (!constraintVerifier.areEqual(lhsConstant, rhsConstant)) {
                checker.report(Result.failure("equality.constraint.unsatisfiable", lhs, rhs),
                        visitorState.getPath().getLeaf());
            }
        }
        return new EqualityConstraint(lhs, rhs, getCurrentLocation());
    }

    public InequalityConstraint createInequalityConstraint(Slot lhs, Slot rhs) {
        if (lhs == null || rhs == null) {
            ErrorReporter.errorAbort("Create inequality constraint with null argument. Subtype: "
                    + lhs + " Supertype: " + rhs);
        }
        if (lhs instanceof ConstantSlot && rhs instanceof ConstantSlot) {
            ConstantSlot lhsConstant = (ConstantSlot) lhs;
            ConstantSlot rhsConstant = (ConstantSlot) rhs;
            if (constraintVerifier.areEqual(lhsConstant, rhsConstant)) {
                checker.report(Result.failure("inequality.constraint.unsatisfiable", lhs, rhs),
                        visitorState.getPath().getLeaf());
            }
        }
        return new InequalityConstraint(lhs, rhs, getCurrentLocation());
    }

    public ComparableConstraint createComparableConstraint(Slot lhs, Slot rhs) {
        if (lhs == null || rhs == null) {
            ErrorReporter.errorAbort("Create comparable constraint with null argument. Subtype: "
                    + lhs + " Supertype: " + rhs);
        }
        if (lhs instanceof ConstantSlot && rhs instanceof ConstantSlot) {
            ConstantSlot lhsConstant = (ConstantSlot) lhs;
            ConstantSlot rhsConstant = (ConstantSlot) rhs;
            if (!constraintVerifier.areComparable(lhsConstant, rhsConstant)) {
                checker.report(Result.failure("comparable.constraint.unsatisfiable", lhs, rhs),
                        visitorState.getPath().getLeaf());
            }
        }
        return new ComparableConstraint(lhs, rhs, getCurrentLocation());
    }

    public ViewpointAdaptationConstraint createViewpointAdaptationConstraint(Slot target, Slot decl,
            Slot result) {
        if (target == null || decl == null || result == null) {
            ErrorReporter.errorAbort(
                    "Create viewpoint adaptation constraint with null argument. Target: " + target
                            + " Decl: " + decl + " Result: " + result);
        }
        return new ViewpointAdaptationConstraint(target, decl, result, getCurrentLocation());
    }

    public PreferenceConstraint createPreferenceConstraint(VariableSlot variable, ConstantSlot goal,
            int weight) {
        if (variable == null || goal == null) {
            ErrorReporter.errorAbort("Create preference constraint with null argument. Variable: "
                    + variable + " Goal: " + goal);
        }
        return new PreferenceConstraint(variable, goal, weight, getCurrentLocation());
    }

    public ExistentialConstraint createExistentialConstraint(Slot slot,
            List<Constraint> ifExistsConstraints, List<Constraint> ifNotExistsConstraints) {
        // TODO: add null checking for argument.
        return new ExistentialConstraint((VariableSlot) slot,
                ifExistsConstraints, ifNotExistsConstraints, getCurrentLocation());
    }

    private void commonArithmeticConstraintInputCheck(ArithmeticOperationKind operation,
            Slot leftOperand, Slot rightOperand, Slot result) {
        if (leftOperand == null || rightOperand == null || result == null) {
            ErrorReporter.errorAbort("Create " + operation
                    + " constraint with null argument. Left Operand: " + leftOperand
                    + " Right Operand: " + rightOperand + " Result: " + result);
        }
    }

    public AdditionConstraint createAdditionConstraint(Slot leftOperand, Slot rightOperand,
            Slot result) {
        commonArithmeticConstraintInputCheck(ArithmeticOperationKind.ADDITION, leftOperand,
                rightOperand, result);
        return new AdditionConstraint(leftOperand, rightOperand, result, getCurrentLocation());
    }

    public SubtractionConstraint createSubtractionConstraint(Slot leftOperand, Slot rightOperand,
            Slot result) {
        commonArithmeticConstraintInputCheck(ArithmeticOperationKind.SUBTRACTION, leftOperand,
                rightOperand, result);
        return new SubtractionConstraint(leftOperand, rightOperand, result, getCurrentLocation());
    }

    public MultiplicationConstraint createMultiplicationConstraint(Slot leftOperand,
            Slot rightOperand, Slot result) {
        commonArithmeticConstraintInputCheck(ArithmeticOperationKind.MULTIPLICATION, leftOperand,
                rightOperand, result);
        return new MultiplicationConstraint(
                leftOperand, rightOperand, result, getCurrentLocation());
    }

    public DivisionConstraint createDivisionConstraint(Slot leftOperand, Slot rightOperand,
            Slot result) {
        commonArithmeticConstraintInputCheck(ArithmeticOperationKind.DIVISION, leftOperand,
                rightOperand, result);
        return new DivisionConstraint(leftOperand, rightOperand, result, getCurrentLocation());
    }

    public ModulusConstraint createModulusConstraint(Slot leftOperand, Slot rightOperand,
            Slot result) {
        commonArithmeticConstraintInputCheck(ArithmeticOperationKind.MODULUS, leftOperand,
                rightOperand, result);
        return new ModulusConstraint(leftOperand, rightOperand, result, getCurrentLocation());
    }

    private AnnotationLocation getCurrentLocation() {
        if (visitorState.getPath() != null) {
            return VariableAnnotator.treeToLocation(inferenceTypeFactory, visitorState.getPath()
                    .getLeaf());
        } else {
            return AnnotationLocation.MISSING_LOCATION;
        }
    }

    public void addSubtypeConstraint(Slot subtype, Slot supertype) {
        if ((subtype instanceof ConstantSlot)
                && realQualHierarchy.getTopAnnotations().contains(((ConstantSlot) subtype).getValue())) {
            addEqualityConstraint(supertype, (ConstantSlot) subtype);
        } else if ((supertype instanceof ConstantSlot)
                && realQualHierarchy.getBottomAnnotations().contains(
                        ((ConstantSlot) supertype).getValue())) {
            addEqualityConstraint(subtype, (ConstantSlot) supertype);
        } else {
            add(createSubtypeConstraint(subtype, supertype));
        }
    }

    public void addEqualityConstraint(Slot lhs, Slot rhs) {
        add(createEqualityConstraint(lhs, rhs));
    }

    public void addInequalityConstraint(Slot lhs, Slot rhs) {
        add(createInequalityConstraint(lhs, rhs));
    }

    public void addComparableConstraint(Slot lhs, Slot rhs) {
        add(createComparableConstraint(lhs, rhs));
    }

    public void addViewpointAdaptationConstraint(Slot target, Slot decl, Slot result) {
        add(createViewpointAdaptationConstraint(target, decl, result));
    }

    public void addPreferenceConstraint(VariableSlot variable, ConstantSlot goal, int weight) {
        add(createPreferenceConstraint(variable, goal, weight));
    }

    public void addExistentialConstraint(Slot slot, List<Constraint> ifExistsConstraints,
            List<Constraint> ifNotExistsConstraints) {
        add(createExistentialConstraint(slot, ifExistsConstraints, ifNotExistsConstraints));
    }

    public void addAdditionConstraint(Slot leftOperand, Slot rightOperand, Slot result) {
        add(createAdditionConstraint(leftOperand, rightOperand, result));
    }

    public void addSubtractionConstraint(Slot leftOperand, Slot rightOperand, Slot result) {
        add(createSubtractionConstraint(leftOperand, rightOperand, result));
    }

    public void addMultiplicationConstraint(Slot leftOperand, Slot rightOperand, Slot result) {
        add(createMultiplicationConstraint(leftOperand, rightOperand, result));
    }

    public void addDivisionConstraint(Slot leftOperand, Slot rightOperand, Slot result) {
        add(createDivisionConstraint(leftOperand, rightOperand, result));
    }

    public void addModulusConstraint(Slot leftOperand, Slot rightOperand, Slot result) {
        add(createModulusConstraint(leftOperand, rightOperand, result));
    }
}
