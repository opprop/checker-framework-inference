package refval.solvers.classic;

import org.checkerframework.javacutil.AnnotationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.sat4j.core.VecInt;

import checkers.inference.InferenceMain;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.Constraint;
import checkers.inference.model.serialization.CnfVecIntSerializer;
import refval.qual.UnknownRefVal;
import refval.util.RefValUtils;

public class RefValSerializer extends CnfVecIntSerializer {
    protected final String datatype;
    private final Set<Integer> touchedSlots = new HashSet<Integer>();
    private boolean isRoot = false;

    public RefValSerializer(String datatype, boolean isRoot) {
        super(InferenceMain.getInstance().getSlotManager());
        this.datatype = datatype;
        this.isRoot = isRoot;
        // System.out.println(datatype);
    }

    @Override
    protected boolean isTop(ConstantSlot constantSlot) {
        AnnotationMirror anno = constantSlot.getValue();
        return annoIsPresented(anno);
    }

    private boolean annoIsPresented(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(anno, UnknownRefVal.class)) {
            return true;
        }
        String[] datatypes;
        if (this.isRoot) {
            datatypes = RefValUtils.getTypeNameRoots(anno);
        } else {
            datatypes = RefValUtils.getTypeNames(anno);
        }

        return Arrays.asList(datatypes).contains(datatype);
    }

    @Override
    public List<VecInt> convertAll(Iterable<Constraint> constraints,
            List<VecInt> results) {
        for (Constraint constraint : constraints) {
            for (VecInt res : constraint.serialize(this)) {
                if (res.size() != 0) {
                    results.add(res);
                }
            }
        }
        return results;
    }

    public boolean isRoot() {
        return this.isRoot;
    }
}
