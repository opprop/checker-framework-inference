package checkers.inference.solver.backend.javasmt.encoder;

import org.sosy_lab.java_smt.api.BooleanFormula;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.javasmt.JavaSmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

/** JavaSMT encoding for equality constraints. */
public class JavaSmtEqualityConstraintEncoder extends JavaSmtAbstractConstraintEncoder
        implements EqualityConstraintEncoder<BooleanFormula> {

    public JavaSmtEqualityConstraintEncoder(
            Lattice lattice, JavaSmtFormatTranslator formatTranslator) {
        super(lattice, formatTranslator);
    }

    @Override
    public BooleanFormula encodeVariable_Variable(VariableSlot left, VariableSlot right) {
        return sameBitVector(left.serialize(formatTranslator), right.serialize(formatTranslator));
    }

    @Override
    public BooleanFormula encodeVariable_Constant(VariableSlot left, ConstantSlot right) {
        return sameBitVector(left.serialize(formatTranslator), right.serialize(formatTranslator));
    }

    @Override
    public BooleanFormula encodeConstant_Variable(ConstantSlot left, VariableSlot right) {
        return sameBitVector(left.serialize(formatTranslator), right.serialize(formatTranslator));
    }
}
