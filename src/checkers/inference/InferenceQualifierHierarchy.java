package checkers.inference;

import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ConstraintManager;
import checkers.inference.model.LubVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.qual.VarAnnot;
import checkers.inference.util.InferenceUtil;
import org.checkerframework.com.google.common.collect.ImmutableMap;
import org.checkerframework.framework.qual.PolymorphicQualifier;
import org.checkerframework.framework.type.ElementQualifierHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.util.AnnotationMirrorSet;
import org.checkerframework.framework.util.QualifierKind;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.org.plumelib.util.StringsPlume;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A qualifier hierarchy that generates constraints rather than evaluating them.  Calls to isSubtype
 * generates subtype and equality constraints between the input types based on the expected subtype
 * relationship (as described by the method signature).
 */
public class InferenceQualifierHierarchy extends ElementQualifierHierarchy {

    private static final String VARANNOT_NAME = VarAnnot.class.getCanonicalName();

    private final InferenceMain inferenceMain = InferenceMain.getInstance();

    private final SlotManager slotMgr;
    private final ConstraintManager constraintMgr;

    public InferenceQualifierHierarchy(
            Collection<Class<? extends Annotation>> qualifierClasses,
            Elements elements
    ) {
        super(qualifierClasses, elements);

        slotMgr = inferenceMain.getSlotManager();
        constraintMgr = inferenceMain.getConstraintManager();
    }

    @Override
    protected Map<QualifierKind, AnnotationMirror> createTopsMap() {
        QualifierKind varAnnotKind = qualifierKindHierarchy.getQualifierKind(VARANNOT_NAME);
        return ImmutableMap.of(varAnnotKind, findTopVarAnnot());
    }

    @Override
    protected Map<QualifierKind, AnnotationMirror> createBottomsMap() {
        QualifierKind varAnnotKind = qualifierKindHierarchy.getQualifierKind(VARANNOT_NAME);
        return ImmutableMap.of(varAnnotKind, findBottomVarAnnot());
    }

    /**
     * @return true if anno is meta-annotated with PolymorphicQualifier
     */
    public static boolean isPolymorphic(AnnotationMirror anno) {
        // This is kind of an expensive way to compute this
        List<? extends AnnotationMirror> metaAnnotations = anno.getAnnotationType().asElement().getAnnotationMirrors();
        for (AnnotationMirror metaAnno : metaAnnotations) {
            if (metaAnno.getAnnotationType().toString().equals(PolymorphicQualifier.class.getCanonicalName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if anno is an instance of @VarAnnot
     */
    public static boolean isVarAnnot(AnnotationMirror anno) {
        return AnnotationUtils.areSameByName(anno, VARANNOT_NAME);
    }

    /**
     * Overridden to prevent isSubtype call by just returning the first annotation.
     *
     * There should at most be 1 annotation on a type.
     *
     */
    @Override
    public AnnotationMirror findAnnotationInSameHierarchy(
            Collection<? extends AnnotationMirror> annos, AnnotationMirror annotationMirror) {

        if (!annos.isEmpty()) {
            final AnnotationMirror anno = isVarAnnot(annotationMirror) ? findVarAnnot(annos)
                                                              : findNonVarAnnot(annos);
            if (anno != null) {
                return anno;
            }
        }

        return null;

    }

    @Override
    public AnnotationMirror findAnnotationInHierarchy(
            Collection<? extends AnnotationMirror> annos, AnnotationMirror top) {

        if (!annos.isEmpty()) {
            final AnnotationMirror anno = isVarAnnot(top) ? findVarAnnot(annos)
                                                          : findNonVarAnnot(annos);
            if (anno != null) {
                return anno;
            }
        }

        return null;
    }

    /**
     * @return the first annotation in annos that is NOT an @VarAnnot
     */
    public static AnnotationMirror findNonVarAnnot(final Iterable<? extends AnnotationMirror> annos) {
        for (AnnotationMirror anno : annos) {
            if (!isVarAnnot(anno)) {
                return anno;
            }
        }

        return null;
    }

    /**
     * @return the first annotation in annos that IS an @VarAnnot
     */
    public static AnnotationMirror findVarAnnot(final Iterable<? extends AnnotationMirror> annos) {
        for (AnnotationMirror anno : annos) {
            if (InferenceMain.isHackMode(anno == null)) {
                continue;
            }

            if (isVarAnnot(anno)) {
                return anno;
            }
        }

        return null;
    }

    @Override
    public boolean isSubtype(final Collection<? extends AnnotationMirror> rhsAnnos,
                             final Collection<? extends AnnotationMirror> lhsAnnos ) {

        final AnnotationMirror rhsVarAnnot = findVarAnnot(rhsAnnos);
        final AnnotationMirror lhsVarAnnot = findVarAnnot(lhsAnnos);

        if (InferenceMain.isHackMode(rhsVarAnnot == null || lhsVarAnnot == null)) {
                InferenceMain.getInstance().logger.info(
                    "Hack:\n"
                  + "    rhs=" + StringsPlume.join(", ", rhsAnnos) + "\n"
                  + "    lhs=" + StringsPlume.join(", ", lhsAnnos ));
                return true;
        }

        assert rhsVarAnnot != null && lhsVarAnnot != null :
                "All types should have exactly 1 VarAnnot!\n"
              + "    rhs=" + StringsPlume.join(", ", rhsAnnos) + "\n"
              + "    lhs=" + StringsPlume.join(", ", lhsAnnos );

        return isSubtype(rhsVarAnnot, lhsVarAnnot);
    }

    @Override
    public boolean isSubtype(final AnnotationMirror subtype, final AnnotationMirror supertype) {

        // NOTE: subtype and supertype are nullable because, for example, in BaseTypeVisitor::checkConstructorInvocation,
        // findAnnotationInSameHierarchy may return null since @VarAnnot and some constant real qualifier
        // are not in the same qualifier hierarchy.
        if (subtype == null || supertype == null || !isVarAnnot(subtype) || !isVarAnnot(supertype)) {
            return true;
        }

        if (supertype.getElementValues().isEmpty()) {
            // Both arguments are varAnnot, but supertype has no slot id.
            // This case may only happen when we check whether a qualifier
            // belongs to the same hierarchy.
            return true;
        }

        final Slot subSlot   = slotMgr.getSlot(subtype);
        final Slot superSlot = slotMgr.getSlot(supertype);

        return constraintMgr.addSubtypeConstraintNoErrorMsg(subSlot, superSlot);
    }

    // Currently, we don't need GLB for VarAnnot. If we need this method in
    // the future, please check the implementation of leastUpperBound for
    // how to create all the constraints.
    @Override
    public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {
        throw new NotImplementedException("InferenceQualifierHierarchy::greatestLowerBound");
    }

    @Override
    public Set<? extends AnnotationMirror> leastUpperBounds(
            Collection<? extends AnnotationMirror> annos1,
            Collection<? extends AnnotationMirror> annos2) {
        if (InferenceMain.isHackMode(annos1.size() != annos2.size())) {
            Set<AnnotationMirror> result = AnnotationUtils.createAnnotationSet();
            for (AnnotationMirror a1 : annos1) {
                for (AnnotationMirror a2 : annos2) {
                    AnnotationMirror lub = leastUpperBound(a1, a2);
                    if (lub != null) {
                        result.add(lub);
                    }
                }
            }
            return result;
        }
        return super.leastUpperBounds(annos1, annos2);
    }

    @Override
    public AnnotationMirror leastUpperBound(final AnnotationMirror a1, final AnnotationMirror a2) {
        if (InferenceMain.isHackMode( (a1 == null || a2 == null))) {
            InferenceMain.getInstance().logger.info(
                    "Hack:\n"
                  + "a1=" + a1 + "\n"
                  + "a2=" + a2);
            return a1 != null ? a1 : a2;
        }
        assert a1 != null && a2 != null : "leastUpperBound accepts only NonNull types! 1 (" + a1 + " ) a2 (" + a2 + ")";

        QualifierHierarchy realQualifierHierarhcy = inferenceMain.getRealTypeFactory().getQualifierHierarchy();
        // for some reason LUB compares all annotations even if they are not in the same sub-hierarchy
        if (!isVarAnnot(a1)) {
            if (!isVarAnnot(a2)) {
                return inferenceMain.getRealTypeFactory().getQualifierHierarchy().leastUpperBound(a1, a2);
            } else {
                return null;
            }
        } else if (!isVarAnnot(a2)) {
            return null;
        }

        // TODO: How to get the path to the CombVariable?
        final Slot slot1 = slotMgr.getSlot(a1);
        final Slot slot2 = slotMgr.getSlot(a2);
        if (slot1 != slot2) {
            if ((slot1 instanceof ConstantSlot) && (slot2 instanceof ConstantSlot)) {
                // If both slots are constant slots, using real qualifier hierarchy to compute the LUB,
                // then return a VarAnnot represent the constant LUB.
                // (Because we passing in two VarAnnots that represent constant slots, so it is consistent
                // to also return a VarAnnot that represents the constant LUB of these two constants.)
                AnnotationMirror realAnno1 = ((ConstantSlot) slot1).getValue();
                AnnotationMirror realAnno2 = ((ConstantSlot) slot2).getValue();

                AnnotationMirror realLub = realQualifierHierarhcy.leastUpperBound(realAnno1, realAnno2);
                Slot constantSlot = slotMgr.createConstantSlot(realLub);
                return slotMgr.getAnnotation(constantSlot);
            } else if (!Collections.disjoint(slot1.getMergedToSlots(), slot2.getMergedToSlots())) {
                // They have common merge variables, return the annotations on one of the common merged variables.
                Slot commonMergedSlot = getOneIntersected(slot1.getMergedToSlots(), slot2.getMergedToSlots());
                return slotMgr.getAnnotation(commonMergedSlot);
            } else if (slot1.isMergedTo(slot2)) {
                // var2 is a merge variable that var1 has been merged to. So just return annotation on var2.
                return slotMgr.getAnnotation(slot2);
            } else if (slot2.isMergedTo(slot1)) {
                // Vice versa.
                return slotMgr.getAnnotation(slot1);
            } else {
                // Create a new LubVariable for var1 and var2.
                final LubVariableSlot mergeVariableSlot = slotMgr.createLubVariableSlot(slot1, slot2);
                constraintMgr.addSubtypeConstraint(slot1, mergeVariableSlot);
                constraintMgr.addSubtypeConstraint(slot2, mergeVariableSlot);

                slot1.addMergedToSlot(mergeVariableSlot);
                slot2.addMergedToSlot(mergeVariableSlot);

                return slotMgr.getAnnotation(mergeVariableSlot);
            }
        } else {
            // They are the same slot.
            return slotMgr.getAnnotation(slot1);
        }
    }

    /**
     * @return The first element found in both set1 and set2. Otherwise return null.
     */
    private <T> T getOneIntersected(Set<T> set1, Set<T> set2) {
        for (T refVar : set1) {
            if (set2.contains(refVar)) {
                return refVar;
            }
        }
        return null;
    }

    /**
     * Find the corresponding {@code VarAnnot} for the real top qualifier.
     *
     * @return a singleton that contains the VarAnnot corresponding to the real top qualifier
     */
    private static AnnotationMirror findTopVarAnnot() {
        AnnotationMirrorSet annos = new AnnotationMirrorSet();
        InferenceMain inferenceMain = InferenceMain.getInstance();
        Set<? extends AnnotationMirror> realTops = inferenceMain.getRealTypeFactory().getQualifierHierarchy().getTopAnnotations();
        SlotManager slotManager = inferenceMain.getSlotManager();
        for (AnnotationMirror top : realTops) {
            ConstantSlot slot = (ConstantSlot) slotManager.getSlot(top);
            if (slot != null) {
                annos.add(slotManager.getAnnotation(slot));
            }
        }

        if (annos.size() != 1) {
            throw new BugInCF(
                    "There should be exactly 1 top qualifier in inference hierarchy"
                            + "( checkers.inference.qual.VarAnnot ).\n"
                            + "Tops found ( " + InferenceUtil.join(annos) + " )"
            );
        }
        return annos.iterator().next();
    }

    /**
     * Find the corresponding {@code VarAnnot} for the real bottom qualifier.
     *
     * @return a singleton that contains the VarAnnot corresponding to the real bottom qualifier
     */
    private static AnnotationMirror findBottomVarAnnot() {
        AnnotationMirrorSet annos = new AnnotationMirrorSet();
        InferenceMain inferenceMain = InferenceMain.getInstance();
        Set<? extends AnnotationMirror> realBottoms = inferenceMain.getRealTypeFactory().getQualifierHierarchy().getBottomAnnotations();
        SlotManager slotManager = inferenceMain.getSlotManager();
        assert slotManager.getSlots().size() > 0;
        for (AnnotationMirror bottom : realBottoms) {
            ConstantSlot slot = (ConstantSlot) slotManager.getSlot(bottom);
            if (slot != null) {
                annos.add(slotManager.getAnnotation(slot));
            }
        }

        if (annos.size() != 1) {
            throw new BugInCF(
                    "There should be exactly 1 bottom qualifier in inference hierarchy"
                            + "( checkers.inference.qual.VarAnnot ).\n"
                            + "Bottoms found ( " + InferenceUtil.join(annos) + " )"
            );
        }
        return annos.iterator().next();
    }

    @Override
    public AnnotationMirror getTopAnnotation(final AnnotationMirror am) {
        if (isVarAnnot(am)) {
            return tops.iterator().next();
        }

        throw new BugInCF("trying to get real top annotation from the inference hierarchy");
    }

    @Override
    public AnnotationMirror getBottomAnnotation(final AnnotationMirror am) {
        if (isVarAnnot(am)) {
            return bottoms.iterator().next();
        }

        throw new BugInCF("trying to get real bottom annotation from the inference hierarchy");
    }
}
