package checkers.inference;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.visitor.AnnotatedTypeScanner;
import org.checkerframework.javacutil.BugInCF;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.Slot;

/**
 * Given a set of replacement mappings (oldSlot -> newSlot), SlotReplacer will replace each oldSlot
 * in an AnnotatedTypeMirror with newSlot.
 *
 * <p>Usage: new SlotReplacer(slotManager) .addReplacement(oldSlot1, newSlot1)
 * .addReplacement(oldSlot2, newSlot2) .addReplacement(oldSlot3, newSlot3)
 * .replaceSlots(atmToUpdate)
 *
 * <p>or new SlotReplacer(slotManager, Arrays.asList( new Replacement(oldSlot1, newSlot2), new
 * Replacement(oldSlot2, newSlot2), new Replacement(oldSlot3, newSlot3) )
 * ).replaceSlots(atmToUpdate)
 */
public class VariableSlotReplacer {
    private final Set<Replacement> replacements = new HashSet<Replacement>();
    private final SlotManager slotManager;
    private final AnnotationMirror varAnnot;
    private final boolean replaceInExistentials;
    private final VariableAnnotator varAnnotator;

    public VariableSlotReplacer(
            final SlotManager slotManager,
            final VariableAnnotator varAnnotator,
            final AnnotationMirror varAnnot,
            boolean replaceInExistentials) {
        this.slotManager = slotManager;
        this.varAnnotator = varAnnotator;
        this.varAnnot = varAnnot;
        this.replaceInExistentials = replaceInExistentials;
    }

    public VariableSlotReplacer(
            final Collection<Replacement> initialReplacements,
            final SlotManager slotManager,
            final VariableAnnotator varAnnotator,
            final AnnotationMirror varAnnot,
            final boolean replaceInExistentials) {
        this(slotManager, varAnnotator, varAnnot, replaceInExistentials);
        replacements.addAll(initialReplacements);
    }

    public VariableSlotReplacer addReplacement(final Slot oldSlot, final Slot newSlot) {
        this.replacements.add(new Replacement(oldSlot, newSlot));
        return this;
    }

    /**
     * For each replacement mapping (oldSlot -> newSlot) configured in this slotReplacer, replace
     * each oldSlot in type with its corresponding newSlot
     *
     * @param type
     */
    public void replaceSlots(final AnnotatedTypeMirror type) {
        type.accept(new BoundReplacementVisitor(), replacements);
    }

    /**
     * Create a copy of type then for each replacement mapping (oldSlot -> newSlot) configured in
     * this slotReplacer, replace each oldSlot in type with its corresponding newSlot
     *
     * @param type
     * @return
     */
    public AnnotatedTypeMirror copyAndReplaceSlots(final AnnotatedTypeMirror type) {
        final AnnotatedTypeMirror copy = type.deepCopy();
        replaceSlots(copy);
        return copy;
    }

    private class BoundReplacementVisitor extends AnnotatedTypeScanner<Void, Set<Replacement>> {

        @Override
        protected Void scan(AnnotatedTypeMirror type, Set<Replacement> replacements) {

            // If we have a lot of Replacements it would make a lot more sense to put them in a
            // map and test them all at once rather than re-traversing.  For now, we use it only
            // with single annotations so I am not worried
            for (final Replacement replacement : replacements) {
                testAndReplace(replacement, type);
            }

            return super.scan(type, replacements);
        }

        protected void testAndReplace(Replacement replacement, AnnotatedTypeMirror type) {
            AnnotationMirror anno = type.getAnnotationInHierarchy(varAnnot);
            if (anno != null) {
                Slot variable = slotManager.getSlot(anno);

                if (slotManager.getSlot(type).equals(replacement.oldSlot)) {
                    final AnnotationMirror newAnnotation =
                            slotManager.getAnnotation(replacement.newSlot);
                    type.replaceAnnotation(newAnnotation);

                } else if (replaceInExistentials && variable instanceof ExistentialVariableSlot) {

                    Slot existentialReplacement =
                            constructExistentialReplacement(
                                    replacement, (ExistentialVariableSlot) variable);
                    if (existentialReplacement != null) {
                        final AnnotationMirror newAnnotation =
                                slotManager.getAnnotation(existentialReplacement);
                        type.replaceAnnotation(newAnnotation);
                    }
                }
            }
        }

        protected Slot constructExistentialReplacement(
                Replacement replacement, ExistentialVariableSlot variable) {
            Slot potential = variable.getPotentialSlot();
            AnnotationMirror potentialAnno = null;

            // alternative may itself be another existential
            Slot alternative = variable.getAlternativeSlot();

            boolean replace = false;

            if (potential.equals(replacement.oldSlot)) {
                potential = replacement.newSlot;
                replace = true;
            }

            if (alternative.equals(replacement.oldSlot)) {
                alternative = replacement.newSlot;
                replace = true;

            } else if (alternative instanceof ExistentialVariableSlot) {
                Slot existentialAlternative =
                        constructExistentialReplacement(
                                replacement, (ExistentialVariableSlot) alternative);

                if (existentialAlternative != null) {
                    alternative = existentialAlternative;
                    replace = true;
                }
            }

            if (replace) {
                return varAnnotator.getOrCreateExistentialVariable(potential, alternative);
            } else {
                return null;
            }
        }
    }

    public static class Replacement {
        public final Slot oldSlot;
        public final Slot newSlot;

        public Replacement(final Slot oldSlot, final Slot newSlot) {
            this.oldSlot = oldSlot;
            this.newSlot = newSlot;

            if (oldSlot == null || newSlot == null) {
                throw new BugInCF("Replacement includes null Slot: \n" + this.toString());
            }
        }

        @Override
        public String toString() {
            return "Replacement( " + oldSlot + " -> " + newSlot + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (obj == null || !(obj.getClass().equals(Replacement.class))) {
                return false;
            }

            final Replacement that = (Replacement) obj;
            return this.oldSlot.equals(that.oldSlot) && this.newSlot.equals(that.newSlot);
        }

        @Override
        public int hashCode() {
            return 31 * (oldSlot.hashCode() + newSlot.hashCode());
        }
    }
}
