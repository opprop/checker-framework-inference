package checkers.inference.solver.backend.javasmt.encoder;

import org.sosy_lab.java_smt.api.BitvectorFormula;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoder;
import checkers.inference.solver.backend.javasmt.JavaSmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

/** Base class for JavaSMT constraint encoders. */
public class JavaSmtAbstractConstraintEncoder extends AbstractConstraintEncoder<BooleanFormula> {

    protected final JavaSmtFormatTranslator formatTranslator;
    protected final BooleanFormulaManager booleanFormulaManager;
    protected final BitvectorFormulaManager bitvectorFormulaManager;

    public JavaSmtAbstractConstraintEncoder(
            Lattice lattice, JavaSmtFormatTranslator formatTranslator) {
        super(
                lattice,
                formatTranslator.getBooleanFormulaManager().makeTrue(),
                formatTranslator.getBooleanFormulaManager().makeFalse());
        this.formatTranslator = formatTranslator;
        this.booleanFormulaManager = formatTranslator.getBooleanFormulaManager();
        this.bitvectorFormulaManager = formatTranslator.getBitvectorFormulaManager();
    }

    protected BooleanFormula sameBitVector(BitvectorFormula left, BitvectorFormula right) {
        return bitvectorFormulaManager.equal(left, right);
    }
}
