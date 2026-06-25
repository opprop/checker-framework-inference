package checkers.inference.solver.backend.javasmt.encoder;

import org.sosy_lab.java_smt.api.BooleanFormula;

import checkers.inference.solver.backend.encoder.AbstractConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.ArithmeticConstraintEncoder;
import checkers.inference.solver.backend.encoder.ComparisonConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.implication.ImplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;
import checkers.inference.solver.backend.javasmt.JavaSmtFormatTranslator;
import checkers.inference.solver.frontend.Lattice;

/** Factory for JavaSMT constraint encoders. */
public class JavaSmtConstraintEncoderFactory
        extends AbstractConstraintEncoderFactory<BooleanFormula, JavaSmtFormatTranslator> {

    public JavaSmtConstraintEncoderFactory(
            Lattice lattice, JavaSmtFormatTranslator formatTranslator) {
        super(lattice, formatTranslator);
    }

    @Override
    public SubtypeConstraintEncoder<BooleanFormula> createSubtypeConstraintEncoder() {
        return new JavaSmtSubtypeConstraintEncoder(lattice, formatTranslator);
    }

    @Override
    public EqualityConstraintEncoder<BooleanFormula> createEqualityConstraintEncoder() {
        return new JavaSmtEqualityConstraintEncoder(lattice, formatTranslator);
    }

    @Override
    public InequalityConstraintEncoder<BooleanFormula> createInequalityConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public ComparableConstraintEncoder<BooleanFormula> createComparableConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public ComparisonConstraintEncoder<BooleanFormula> createComparisonConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public PreferenceConstraintEncoder<BooleanFormula> createPreferenceConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public CombineConstraintEncoder<BooleanFormula> createCombineConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public ExistentialConstraintEncoder<BooleanFormula> createExistentialConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public ImplicationConstraintEncoder<BooleanFormula> createImplicationConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    @Override
    public ArithmeticConstraintEncoder<BooleanFormula> createArithmeticConstraintEncoder() {
        return unsupportedConstraintEncoder();
    }

    private <T> T unsupportedConstraintEncoder() {
        /*
         * AbstractFormatTranslator treats a null encoder as "unsupported by this backend".
         * JavaSmtSolver rejects unsupported hard constraints instead of solving without them.
         */
        return null;
    }
}
