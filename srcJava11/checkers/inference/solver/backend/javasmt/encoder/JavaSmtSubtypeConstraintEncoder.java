package checkers.inference.solver.backend.javasmt.encoder;

import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BooleanFormula;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.backend.javasmt.JavaSmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

/** JavaSMT encoding for subtype constraints. */
public class JavaSmtSubtypeConstraintEncoder extends JavaSmtAbstractConstraintEncoder
        implements SubtypeConstraintEncoder<BooleanFormula> {

    public JavaSmtSubtypeConstraintEncoder(
            Lattice lattice, JavaSmtFormatTranslator formatTranslator) {
        super(lattice, formatTranslator);
    }

    private BooleanFormula encode(Slot subtype, Slot supertype) {
        BitvectorFormula subtypeBv = subtype.serialize(formatTranslator);
        BitvectorFormula supertypeBv = supertype.serialize(formatTranslator);

        BooleanFormula subset =
                sameBitVector(bitvectorFormulaManager.and(subtypeBv, supertypeBv), subtypeBv);
        BooleanFormula union =
                sameBitVector(bitvectorFormulaManager.or(subtypeBv, supertypeBv), supertypeBv);
        return booleanFormulaManager.and(subset, union);
    }

    @Override
    public BooleanFormula encodeVariable_Variable(VariableSlot subtype, VariableSlot supertype) {
        return encode(subtype, supertype);
    }

    @Override
    public BooleanFormula encodeVariable_Constant(VariableSlot subtype, ConstantSlot supertype) {
        return encode(subtype, supertype);
    }

    @Override
    public BooleanFormula encodeConstant_Variable(ConstantSlot subtype, VariableSlot supertype) {
        return encode(subtype, supertype);
    }
}
