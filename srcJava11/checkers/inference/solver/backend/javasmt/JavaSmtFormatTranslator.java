package checkers.inference.solver.backend.javasmt;

import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ComparisonVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.LubVariableSlot;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.SourceVariableSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.AbstractFormatTranslator;
import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.backend.javasmt.encoder.JavaSmtConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;

/** Translates CFI slots and constraints to JavaSMT bit-vector formulas. */
public class JavaSmtFormatTranslator
        extends AbstractFormatTranslator<BitvectorFormula, BooleanFormula, BigInteger> {

    private SolverContext context;
    private FormulaManager formulaManager;
    private BitvectorFormulaManager bitvectorFormulaManager;
    private BooleanFormulaManager booleanFormulaManager;
    private final Map<Integer, BitvectorFormula> serializedSlots;
    private final JavaSmtBitVectorCodec bitVectorCodec;

    public JavaSmtFormatTranslator(Lattice lattice) {
        super(lattice);
        serializedSlots = new HashMap<>();
        bitVectorCodec = new JavaSmtBitVectorCodec(lattice);
    }

    public final void init(SolverContext context) {
        this.context = context;
        formulaManager = context.getFormulaManager();
        bitvectorFormulaManager = formulaManager.getBitvectorFormulaManager();
        booleanFormulaManager = formulaManager.getBooleanFormulaManager();
        finishInitializingEncoders();
    }

    public BooleanFormulaManager getBooleanFormulaManager() {
        return booleanFormulaManager;
    }

    public BitvectorFormulaManager getBitvectorFormulaManager() {
        return bitvectorFormulaManager;
    }

    public BitvectorFormula getSerializedSlot(int slotId) {
        return serializedSlots.get(slotId);
    }

    private BitvectorFormula serializeVariableSlot(VariableSlot slot) {
        int slotId = slot.getId();
        BitvectorFormula serializedSlot = serializedSlots.get(slotId);
        if (serializedSlot == null) {
            serializedSlot =
                    bitvectorFormulaManager.makeVariable(
                            bitVectorCodec.getBitVectorSize(), String.valueOf(slotId));
            serializedSlots.put(slotId, serializedSlot);
        }
        return serializedSlot;
    }

    private BitvectorFormula serializeConstantSlot(ConstantSlot slot) {
        int slotId = slot.getId();
        BitvectorFormula serializedSlot = serializedSlots.get(slotId);
        if (serializedSlot == null) {
            serializedSlot =
                    bitvectorFormulaManager.makeBitvector(
                            bitVectorCodec.getBitVectorSize(),
                            bitVectorCodec.encodeConstantAM(slot.getValue()));
            serializedSlots.put(slotId, serializedSlot);
        }
        return serializedSlot;
    }

    @Override
    protected ConstraintEncoderFactory<BooleanFormula> createConstraintEncoderFactory() {
        return new JavaSmtConstraintEncoderFactory(lattice, this);
    }

    @Override
    public BitvectorFormula serialize(SourceVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(ConstantSlot slot) {
        return serializeConstantSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(ExistentialVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(RefinementVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(CombVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(LubVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(ArithmeticVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public BitvectorFormula serialize(ComparisonVariableSlot slot) {
        return serializeVariableSlot(slot);
    }

    @Override
    public AnnotationMirror decodeSolution(
            BigInteger solution, ProcessingEnvironment processingEnvironment) {
        return bitVectorCodec.decodeNumeralValue(solution, processingEnvironment);
    }
}
