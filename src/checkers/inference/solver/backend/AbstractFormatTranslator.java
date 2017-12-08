package checkers.inference.solver.backend;

import com.microsoft.z3.Optimize;
import checkers.inference.InferenceMain;
import checkers.inference.model.AdditionConstraint;
import checkers.inference.model.BinaryConstraint;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.CombineConstraint;
import checkers.inference.model.ComparableConstraint;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.DivisionConstraint;
import checkers.inference.model.EqualityConstraint;
import checkers.inference.model.ExistentialConstraint;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.InequalityConstraint;
import checkers.inference.model.ModulusConstraint;
import checkers.inference.model.MultiplicationConstraint;
import checkers.inference.model.PreferenceConstraint;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.SubtractionConstraint;
import checkers.inference.model.SubtypeConstraint;
import checkers.inference.model.TernaryConstraint;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.ConstraintEncoderCoordinator;
import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.binary.BinaryConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.AdditionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.DivisionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.ModulusConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.MultiplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.SubtractionConstraintEncoder;
import checkers.inference.solver.backend.encoder.ternary.TernaryConstraintEncoder;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;

/**
 * Abstract base class for all concrete {@link FormatTranslator}.
 *
 * Class {@code AbstractFormatTranslator} provides default implementation for both serializing
 * {@link checkers.inference.model.Slot slot} and {@link checkers.inference.model.Constraint constraint}:
 * <p>
 * {@link checkers.inference.model.Slot Slot} serialization methods does nothing but returns null.
 * Subclasses of {@code AbstractFormatTranslator} should override corresponding {@code Slot}
 * serialization methods if subclasses have concrete serialization logic.
 * <p>
 * {@link checkers.inference.model.Constraint Constraint} serialization methods first check
 * whether corresponding encoder is null. If yes, returns null as the encoding. Otherwise, delegates
 * encoding job to that encoder.
 * <p>
 * Subclasses of {@code AbstractFormatTranslator} need to override method
 * {@link #createConstraintEncoderFactory(ConstraintVerifier)} to create the concrete {@code
 * ConstraintEncoderFactory}. Then at the last step of initializing subclasses of {@code AbstractFormatTranslator},
 * {@link #finishInitializingEncoders()} must be called in order to finish initializing encoders.
 * The reason is: concrete {@link ConstraintEncoderFactory} may depend on some fields in subclasses
 * of {@link AbstractFormatTranslator}.
 * <p>
 * For example, {@link checkers.inference.solver.backend.maxsat.encoder.MaxSATConstraintEncoderFactory}
 * depends on {@link checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator#typeToInt typeToInt}
 * filed in {@link checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator}. So only after those
 * dependant fields are initialized in subclasses constructors, encoders can be then initialized.
 * Calling {@link #finishInitializingEncoders()} at the last step of initialization makes sure all the
 * dependant fields are already initialized.
 * <p>
 * In terms of "last step of initialization", different {@code FormatTranslator}s have different definitions.
 * For {@link checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator} and
 * {@link checkers.inference.solver.backend.logiql.LogiQLFormatTranslator}, it's at the end of the
 * subclass constructor; While for {@link checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator},
 * it's at the end of {@link checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator#initSolver(Optimize)}.
 * The general guideline is that {@link #finishInitializingEncoders() finishInitializingEncoders()} call
 * should always precede actual solving process.
 *
 * @see ConstraintEncoderFactory
 * @see #finishInitializingEncoders()
 * @see checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator
 * @see checkers.inference.solver.backend.logiql.LogiQLFormatTranslator
 * @see checkers.inference.solver.backend.z3.Z3BitVectorFormatTranslator
 */
public abstract class AbstractFormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT>
        implements FormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT>{

    /**
     * {@link Lattice} that is used by subclasses during format translation.
     */
    protected final Lattice lattice;

    /**
     * {@link ConstraintVerifier} that is used to create concrete {@link ConstraintEncoderFactory}
     */
    private final ConstraintVerifier verifier;

    /**
     * {@code SubtypeConstraintEncoder} to which encoding of {@link SubtypeConstraint} is delegated.
     */
    protected SubtypeConstraintEncoder<ConstraintEncodingT> subtypeConstraintEncoder;

    /**
     * {@code EqualityConstraintEncoder} to which encoding of {@link EqualityConstraint} is delegated.
     */
    protected EqualityConstraintEncoder<ConstraintEncodingT> equalityConstraintEncoder;

    /**
     * {@code InequalityConstraintEncoder} to which encoding of {@link InequalityConstraint} is delegated.
     */
    protected InequalityConstraintEncoder<ConstraintEncodingT> inequalityConstraintEncoder;

    /**
     * {@code ComparableConstraintEncoder} to which encoding of {@link ComparableConstraint} is delegated.
     */
    protected ComparableConstraintEncoder<ConstraintEncodingT> comparableConstraintEncoder;

    /**
     * {@code PreferenceConstraintEncoder} to which encoding of {@link PreferenceConstraint} is delegated.
     */
    protected PreferenceConstraintEncoder<ConstraintEncodingT> preferenceConstraintEncoder;

    /**
     * {@code CombineConstraintEncoder} to which encoding of {@link CombineConstraint} is delegated.
     */
    protected CombineConstraintEncoder<ConstraintEncodingT> combineConstraintEncoder;

    /**
     * {@code ExistentialConstraintEncoder} to which encoding of {@link ExistentialConstraint} is delegated.
     */
    protected ExistentialConstraintEncoder<ConstraintEncodingT> existentialConstraintEncoder;

    /**
     * {@code AdditionConstraintEncoder} to which encoding of {@link AdditionConstraint} is delegated.
     */
    protected AdditionConstraintEncoder<ConstraintEncodingT> additionConstraintEncoder;

    /**
     * {@code SubtractionConstraintEncoder} to which encoding of {@link SubtractionConstraint} is delegated.
     */
    protected SubtractionConstraintEncoder<ConstraintEncodingT> subtractionConstraintEncoder;

    /**
     * {@code MultiplicationConstraintEncoder} to which encoding of {@link MultiplicationConstraint} is delegated.
     */
    protected MultiplicationConstraintEncoder<ConstraintEncodingT> multiplicationConstraintEncoder;

    /**
     * {@code DivisionConstraintEncoder} to which encoding of {@link DivisionConstraint} is delegated.
     */
    protected DivisionConstraintEncoder<ConstraintEncodingT> divisionConstraintEncoder;

    /**
     * {@code ModulusConstraintEncoder} to which encoding of {@link ModulusConstraint} is delegated.
     */
    protected ModulusConstraintEncoder<ConstraintEncodingT> modulusConstraintEncoder;

    public AbstractFormatTranslator(Lattice lattice) {
        this.lattice = lattice;
        this.verifier = InferenceMain.getInstance().getConstraintManager().getConstraintVerifier();
    }

    /**
     * Finishes initializing encoders for subclasses of {@code AbstractFormatTranslator}. Subclasses of
     * {@code AbstractFormatTranslator} MUST call this method to finish initializing encoders at the end
     * of initialization phase. See Javadoc on {@link AbstractFormatTranslator} to see what the last
     * step of initialization phase means and why the encoder creation steps are separate out from constructor
     * {@link AbstractFormatTranslator#AbstractFormatTranslator(Lattice, ConstraintVerifier)}
     */
    protected void finishInitializingEncoders() {
        final ConstraintEncoderFactory<ConstraintEncodingT> encoderFactory = createConstraintEncoderFactory(verifier);
        subtypeConstraintEncoder = encoderFactory.createSubtypeConstraintEncoder();
        equalityConstraintEncoder = encoderFactory.createEqualityConstraintEncoder();
        inequalityConstraintEncoder = encoderFactory.createInequalityConstraintEncoder();
        comparableConstraintEncoder = encoderFactory.createComparableConstraintEncoder();
        preferenceConstraintEncoder = encoderFactory.createPreferenceConstraintEncoder();
        combineConstraintEncoder = encoderFactory.createCombineConstraintEncoder();
        existentialConstraintEncoder = encoderFactory.createExistentialConstraintEncoder();
        additionConstraintEncoder = encoderFactory.createAdditionConstraintEncoder();
        subtractionConstraintEncoder = encoderFactory.createSubtractionConstraintEncoder();
        multiplicationConstraintEncoder = encoderFactory.createMultiplicationConstraintEncoder();
        divisionConstraintEncoder = encoderFactory.createDivisionConstraintEncoder();
        modulusConstraintEncoder = encoderFactory.createModulusConstraintEncoder();
    }

    /**
     * Creates concrete implementation of {@link ConstraintEncoderFactory}. Subclasses should implement this method
     * to provide their concrete {@code ConstraintEncoderFactory}.
     *
     * @param verifier {@link ConstraintVerifier} to pass to {@code ConstraintEncoderFactory}
     * @return Concrete implementation of {@link ConstraintEncoderFactory} for a particular {@link SolverType}
     */
    protected abstract ConstraintEncoderFactory<ConstraintEncodingT> createConstraintEncoderFactory(ConstraintVerifier verifier);

    // Dispatches via the Coordinator only if the encoder is instantiated
    private ConstraintEncodingT dispatch(BinaryConstraint constraint,
            BinaryConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder == null ? null : ConstraintEncoderCoordinator.dispatch(constraint, encoder);
    }

    private ConstraintEncodingT dispatch(TernaryConstraint constraint,
            TernaryConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder == null ? null : ConstraintEncoderCoordinator.dispatch(constraint, encoder);
    }

    @Override
    public ConstraintEncodingT serialize(SubtypeConstraint constraint) {
        return dispatch(constraint, subtypeConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(EqualityConstraint constraint) {
        return dispatch(constraint, equalityConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(InequalityConstraint constraint) {
        return dispatch(constraint, inequalityConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ComparableConstraint constraint) {
        return dispatch(constraint, comparableConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(PreferenceConstraint constraint) {
        return preferenceConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.redirect(constraint, preferenceConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ExistentialConstraint constraint) {
        return existentialConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.redirect(constraint, existentialConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(CombineConstraint combineConstraint) {
        return dispatch(combineConstraint, combineConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(AdditionConstraint additionConstraint) {
        return dispatch(additionConstraint, additionConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(SubtractionConstraint subtractionConstraint) {
        return dispatch(subtractionConstraint, subtractionConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(MultiplicationConstraint multiplicationConstraint) {
        return dispatch(multiplicationConstraint, multiplicationConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(DivisionConstraint divisionConstraint) {
        return dispatch(divisionConstraint, divisionConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ModulusConstraint modulusConstraint) {
        return dispatch(modulusConstraint, modulusConstraintEncoder);
    }

    @Override
    public SlotEncodingT serialize(VariableSlot slot) {
        return null;
    }

    @Override
    public SlotEncodingT serialize(ConstantSlot slot) {
        return null;
    }

    @Override
    public SlotEncodingT serialize(ExistentialVariableSlot slot) {
        return null;
    }

    @Override
    public SlotEncodingT serialize(RefinementVariableSlot slot) {
        return null;
    }

    @Override
    public SlotEncodingT serialize(CombVariableSlot slot) {
        return null;
    }
}
