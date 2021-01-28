package checkers.inference.solver.backend.maxsat.encoder;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Slot;
import checkers.inference.solver.backend.encoder.binary.InequalityConstraintEncoder;
import checkers.inference.solver.backend.maxsat.MathUtils;
import checkers.inference.solver.backend.maxsat.VectorUtils;
import checkers.inference.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

public class MaxSATInequalityConstraintEncoder extends MaxSATAbstractConstraintEncoder implements InequalityConstraintEncoder<VecInt[]> {

    public MaxSATInequalityConstraintEncoder(Lattice lattice, Map<AnnotationMirror, Integer> typeToInt) {
        super(lattice, typeToInt);
    }

    @Override
    public VecInt[] encodeVariable_Variable(Slot fst, Slot snd) {
        // a <=> !b which is the same as (!a v !b) & (b v a)
        VecInt[] result = new VecInt[lattice.numTypes * 2];
        int i = 0;
        for (AnnotationMirror type : lattice.allTypes) {
            if (lattice.allTypes.contains(type)) {
                result[i++] = VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(fst.getId(), typeToInt.get(type), lattice),
                        -MathUtils.mapIdToMatrixEntry(snd.getId(), typeToInt.get(type), lattice));
                result[i++] = VectorUtils.asVec(
                        MathUtils.mapIdToMatrixEntry(snd.getId(), typeToInt.get(type), lattice),
                        MathUtils.mapIdToMatrixEntry(fst.getId(), typeToInt.get(type), lattice));
            }
        }
        return result;
    }

    @Override
    public VecInt[] encodeVariable_Constant(Slot fst, ConstantSlot snd) {
        return encodeConstant_Variable(snd, fst);
    }

    @Override
    public VecInt[] encodeConstant_Variable(ConstantSlot fst, Slot snd) {
        if (lattice.allTypes.contains(fst.getValue())) {
            return VectorUtils.asVecArray(
                    -MathUtils.mapIdToMatrixEntry(snd.getId(), typeToInt.get(fst.getValue()), lattice));
        } else {
            return emptyValue;
        }
    }
}
