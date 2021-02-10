package dataflow.solvers.backend.z3;

import checkers.inference.InferenceMain;
import checkers.inference.SlotManager;
import checkers.inference.model.ConstantSlot;
import checkers.inference.solver.backend.z3.Z3BitVectorCodec;


import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import dataflow.DataflowAnnotatedTypeFactory;
import dataflow.qual.DataFlowInferenceBottom;
import dataflow.qual.DataFlowTop;
import dataflow.util.DataflowUtils;

/**
 * The Z3 bit vector codec for dataflow type system.
 */
public class DataflowZ3BitVectorCodec implements Z3BitVectorCodec {

    /** The real annotated type factory. */
    private final DataflowAnnotatedTypeFactory realATF;
    /** The encoding key array containing java types exist in typeNames. */
    private final String[] encodingKeyArr;
    /** The encoding value array containing encoded bits for each java type in {@code encodingKeyArr}. */
    private final BigInteger[] encodingValArr;
    /** The root encoding key array containing java types exist in typeNameRoots. */
    private final String[] rootEncodingKeyArr;
    /** The root encoding value array containing encoded bits for each java type in {@code rootEncodingKeyArr}. */
    private final BigInteger[] rootEncodingValArr;
    /** The slot manager. */
    private final SlotManager slotManager;

    DataflowZ3BitVectorCodec() {
        this.realATF = (DataflowAnnotatedTypeFactory) InferenceMain.getInstance().getRealTypeFactory();
        this.slotManager = InferenceMain.getInstance().getSlotManager();
        this.encodingKeyArr = createEncodingKeyArr(false);
        this.rootEncodingKeyArr = createEncodingKeyArr(true);
        this.encodingValArr = createEncodingValArr(0, this.encodingKeyArr.length);
        this.rootEncodingValArr = createEncodingValArr(this.encodingKeyArr.length, this.rootEncodingKeyArr.length);
    }

    /**
     * Traverse all the constant slots to get the values in typeNames/typeNameRoots of each
     * {@code @DataFlow} annotation as the elements of encoding key array.
     * @return the encoding key array
     */
    private String[] createEncodingKeyArr(boolean root) {
        List<ConstantSlot> slots = this.slotManager
            .getConstantSlots();
        Set<String> set = new HashSet<>();
        String[] types;
        for (ConstantSlot each : slots) {
            AnnotationMirror am = each.getValue();
            if (AnnotationUtils.areSameByClass(am, DataFlowInferenceBottom.class) ||
                AnnotationUtils.areSameByClass(am, DataFlowTop.class)) {
                continue;
            }
            if (root) {
                types = DataflowUtils.getTypeNameRoots(am);
            } else {
                types = DataflowUtils.getTypeNames(am);
            }
            set.addAll(Arrays.asList(types));
        }
        String[] encodingArr = new String[set.size()];
        int i = 0;
        for ( String entry : set) {
            encodingArr[i] = entry;
            i++;
        }
        return encodingArr;
    }

    /**
     * Create both encoding value array and root encoding value array.
     * @param ordinal the start ordinal
     * @param size the size of the output array
     * @return the encoded array
     */
    private BigInteger[] createEncodingValArr(int ordinal, int size) {
        BigInteger[] encodingValArr = new BigInteger[size];
        for (int i = 0; i < size; i++) {
            BigInteger encode = BigInteger.ZERO.setBit(ordinal);
            encodingValArr[i] = encode;
            ordinal++;
        }
        return encodingValArr;
    }

    @Override
    public int getFixedBitVectorSize() {
        if ((this.encodingKeyArr.length / 2 + this.rootEncodingKeyArr.length / 2) > Integer.MAX_VALUE / 2) {
            throw new BugInCF("(encodingKeyArr + rootEncodingKeyArr)'s size is too big.");
        }
        return this.encodingKeyArr.length + this.rootEncodingKeyArr.length;
    }

    @Override
    public BigInteger encodeConstantAM(AnnotationMirror am) {
        if (AnnotationUtils.areSameByClass(am, DataFlowTop.class)) {
            return BigInteger.valueOf(-1);
        }
        if (AnnotationUtils.areSameByClass(am, DataFlowInferenceBottom.class)) {
            return BigInteger.valueOf(0);
        }
        String[] typeNames = DataflowUtils.getTypeNames(am);
        String[] typeNameRoots = DataflowUtils.getTypeNameRoots(am);
        BigInteger encode = BigInteger.ZERO;
        for (String each : typeNames) {
            int i = ArrayUtils.indexOf(this.encodingKeyArr, each);
            encode = encode.or(this.encodingValArr[i]);
        }
        for (String each : typeNameRoots) {
            int i = ArrayUtils.indexOf(this.rootEncodingKeyArr, each);
            encode = encode.or(this.rootEncodingValArr[i]);
        }
        return encode;
    }

    @Override
    public AnnotationMirror decodeNumeralValue(BigInteger numeralValue,
        ProcessingEnvironment processingEnvironment) {
        Set<String> typeNamesSet;
        Set<String> typeNameRootsSet;
        if (numeralValue.equals(BigInteger.valueOf(-1))) {
            return DataflowUtils.createDataflowTop(realATF.getProcessingEnv());
        }
        if (numeralValue.equals(BigInteger.valueOf(0))) {
            return DataflowUtils.createDataflowBottom(realATF.getProcessingEnv());
        }
        typeNamesSet = find(numeralValue, this.encodingKeyArr, this.encodingValArr);
        typeNameRootsSet = find(numeralValue, this.rootEncodingKeyArr, this.rootEncodingValArr);
        return DataflowUtils.createDataflowAnnotationWithRoots(typeNamesSet, typeNameRootsSet, processingEnvironment);
    }

    /**
     * Find the set of java types in string representation using the encoded numeral value.
     * @param numeralValue the encoded bits
     * @param keyArr the (root) encoding key array
     * @param valArr the (root) encoding value array
     * @return the decoded set of java types
     */
    private Set<String> find(BigInteger numeralValue, String[] keyArr, BigInteger[] valArr) {
        Set<String> set = new HashSet<>();
        for(int i = 0; i < keyArr.length; i++) {
            String typeName = keyArr[i];
            BigInteger value = valArr[i];
            if (value.and(numeralValue).equals(value)) {
                set.add(typeName);
                if(numeralValue.equals(value)) {
                    break;
                }
            }
        }
        return set;
    }
}
