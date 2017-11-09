package checkers.inference.solver.backend.maxsat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.solver.backend.AbstractFormatTranslator;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATComparableConstraintEncoder;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATEqualityConstraintEncoder;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATInequalityConstraintEncoder;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATPreferenceConstraintEncoder;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATSubtypeConstraintEncoder;
import checkers.inference.util.ConstraintVerifier;
import org.checkerframework.javacutil.AnnotationUtils;
import org.sat4j.core.VecInt;

import checkers.inference.solver.frontend.Lattice;

/**
 * MaxSatFormatTranslator converts constraint into array of VecInt as clauses.
 * 
 * @author jianchu
 *
 */

public class MaxSatFormatTranslator extends AbstractFormatTranslator<VecInt[], VecInt[], Integer> {

    /**
     * typeToInt maps each type qualifier to an unique integer value starts from
     * 0 on continuous basis.
     */
    protected final Map<AnnotationMirror, Integer> typeToInt;

    /**
     * intToType maps an integer value to each type qualifier, which is a
     * reversed map of typeToInt.
     */
    protected final Map<Integer, AnnotationMirror> intToType;

    public MaxSatFormatTranslator(Lattice lattice, ConstraintVerifier verifier) {
        super(lattice, verifier);
        // Initialize mappings between type and int.
        Map<AnnotationMirror, Integer>typeToIntRes = AnnotationUtils.createAnnotationMap();
        Map<Integer, AnnotationMirror> intToTypeRes = new HashMap<Integer, AnnotationMirror>();

        int curInt = 0;
        for (AnnotationMirror type : lattice.allTypes) {
            typeToIntRes.put(type, curInt);
            intToTypeRes.put(curInt, type);
            curInt ++;
        }

        typeToInt = Collections.unmodifiableMap(typeToIntRes);
        intToType = Collections.unmodifiableMap(intToTypeRes);
        postInit();
    }

    /**
     * generate well form clauses such that there is one and only one beta value
     * can be true.
     *
     * @param clauses
     */
    protected void generateOneHotClauses(List<VecInt> clauses, Integer varSlotId) {
        int[] leastOneIsTrue = new int[lattice.numTypes];
        for (Integer i : intToType.keySet()) {
            leastOneIsTrue[i] = MathUtils.mapIdToMatrixEntry(varSlotId, i.intValue(), lattice);
        }
        clauses.add(VectorUtils.asVec(leastOneIsTrue));
        List<Integer> varList = new ArrayList<Integer>(intToType.keySet());
        for (int i = 0; i < varList.size(); i++) {
            for (int j = i + 1; j < varList.size(); j++) {
                VecInt vecInt = new VecInt(2);
                vecInt.push(-MathUtils.mapIdToMatrixEntry(varSlotId, varList.get(i), lattice));
                vecInt.push(-MathUtils.mapIdToMatrixEntry(varSlotId, varList.get(j), lattice));
                clauses.add(vecInt);
            }
        }
    }

    @Override
    protected MaxSATSubtypeConstraintEncoder createSubtypeConstraintEncoder(ConstraintVerifier verifier) {
        return new MaxSATSubtypeConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    protected MaxSATEqualityConstraintEncoder createEqualityConstraintEncoder(ConstraintVerifier verifier) {
        return new MaxSATEqualityConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    protected MaxSATInequalityConstraintEncoder createInequalityConstraintEncoder(ConstraintVerifier verifier) {
        return new MaxSATInequalityConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    protected MaxSATComparableConstraintEncoder createComparableConstraintEncoder(ConstraintVerifier verifier) {
        return new MaxSATComparableConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    protected MaxSATPreferenceConstraintEncoder createPreferenceConstraintEncoder(ConstraintVerifier verifier) {
        return new MaxSATPreferenceConstraintEncoder(lattice, verifier, typeToInt);
    }

    @Override
    public AnnotationMirror decodeSolution(Integer var, ProcessingEnvironment processingEnvironment) {
        return intToType.get(MathUtils.getIntRep(var, lattice));
    }

}
