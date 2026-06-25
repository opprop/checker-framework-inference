package checkers.inference.solver.backend.javasmt;

import org.checkerframework.javacutil.AnnotationMirrorMap;
import org.checkerframework.javacutil.BugInCF;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import checkers.inference.solver.frontend.Lattice;

/** Encodes lattice qualifiers as bit vectors for the JavaSMT backend. */
public class JavaSmtBitVectorCodec {

    private final int bitVectorSize;
    private final Map<AnnotationMirror, BigInteger> typeToValue;
    private final Map<BigInteger, AnnotationMirror> valueToType;

    public JavaSmtBitVectorCodec(Lattice lattice) {
        bitVectorSize = Math.max(1, lattice.numTypes);
        Map<AnnotationMirror, Integer> typeToBitIndex = new AnnotationMirrorMap<>();

        int index = 0;
        for (AnnotationMirror type : lattice.allTypes) {
            typeToBitIndex.put(type, index);
            index++;
        }

        Map<AnnotationMirror, BigInteger> typeToValueBuilder = new AnnotationMirrorMap<>();
        Map<BigInteger, AnnotationMirror> valueToTypeBuilder = new HashMap<>();
        for (AnnotationMirror type : lattice.allTypes) {
            BigInteger encoded = BigInteger.ZERO;
            for (AnnotationMirror subtype : lattice.subType.get(type)) {
                encoded = encoded.setBit(typeToBitIndex.get(subtype));
            }
            typeToValueBuilder.put(type, encoded);
            valueToTypeBuilder.put(encoded, type);
        }

        typeToValue = typeToValueBuilder;
        valueToType = valueToTypeBuilder;
    }

    public int getBitVectorSize() {
        return bitVectorSize;
    }

    public BigInteger encodeConstantAM(AnnotationMirror annotationMirror) {
        BigInteger encoded = typeToValue.get(annotationMirror);
        if (encoded == null) {
            throw new BugInCF("No JavaSMT bit-vector encoding for " + annotationMirror);
        }
        return encoded;
    }

    public AnnotationMirror decodeNumeralValue(
            BigInteger numeralValue, ProcessingEnvironment processingEnvironment) {
        AnnotationMirror decoded = valueToType.get(numeralValue);
        if (decoded == null) {
            throw new BugInCF("No qualifier maps to JavaSMT bit-vector value " + numeralValue);
        }
        return decoded;
    }
}
