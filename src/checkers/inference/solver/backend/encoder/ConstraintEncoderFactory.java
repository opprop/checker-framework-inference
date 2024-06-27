package checkers.inference.solver.backend.encoder;

import checkers.inference.solver.backend.encoder.binary.ComparableConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.EqualityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.encoder.existential.ExistentialConstraintEncoder;
import checkers.inference.solver.backend.encoder.implication.ImplicationConstraintEncoder;
import checkers.inference.solver.backend.encoder.preference.PreferenceConstraintEncoder;

/**
 * Factory that creates constraint encoders.
 *
 * <p>Right now, {@link ConstraintEncoderFactory} interface supports creation of these encoders:
 *
 * <ul>
 *   <li>{@link SubtypeConstraintEncoder}
 *   <li>{@link EqualityConstraintEncoder}
 *   <li>{@link InequalityConstraintEncoder}
 *   <li>{@link ComparableConstraintEncoder}
 *   <li>{@link ComparisonConstraintEncoder}
 *   <li>{@link PreferenceConstraintEncoder}
 *   <li>{@link CombineConstraintEncoder}
 *   <li>{@link ExistentialConstraintEncoder}
 *   <li>{@link ImplicationConstraintEncoder}
 *   <li>{@link ArithmeticConstraintEncoder}
 * </ul>
 *
 * <p>User of this interface is {@link checkers.inference.solver.backend.AbstractFormatTranslator}
 * and its subclasses.
 *
 * @see checkers.inference.solver.backend.AbstractFormatTranslator
 */
public interface ConstraintEncoderFactory<ConstraintEncodingT> {

    SubtypeConstraintEncoder<ConstraintEncodingT> createSubtypeConstraintEncoder();

    EqualityConstraintEncoder<ConstraintEncodingT> createEqualityConstraintEncoder();

    InequalityConstraintEncoder<ConstraintEncodingT> createInequalityConstraintEncoder();

    ComparableConstraintEncoder<ConstraintEncodingT> createComparableConstraintEncoder();

    ComparisonConstraintEncoder<ConstraintEncodingT> createComparisonConstraintEncoder();

    PreferenceConstraintEncoder<ConstraintEncodingT> createPreferenceConstraintEncoder();

    CombineConstraintEncoder<ConstraintEncodingT> createCombineConstraintEncoder();

    ExistentialConstraintEncoder<ConstraintEncodingT> createExistentialConstraintEncoder();

    ImplicationConstraintEncoder<ConstraintEncodingT> createImplicationConstraintEncoder();

    ArithmeticConstraintEncoder<ConstraintEncodingT> createArithmeticConstraintEncoder();
}
