package checkers.inference;

import checkers.inference.util.SlotDefaultTypeResolver;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import checkers.inference.model.LubVariableSlot;

import com.sun.tools.javac.util.Pair;

import checkers.inference.model.AnnotationLocation;
import checkers.inference.model.ArithmeticVariableSlot;
import checkers.inference.model.CombVariableSlot;
import checkers.inference.model.ComparisonVariableSlot;
import checkers.inference.model.ConstantSlot;
import checkers.inference.model.ExistentialVariableSlot;
import checkers.inference.model.RefinementVariableSlot;
import checkers.inference.model.Slot;
import checkers.inference.model.SourceVariableSlot;
import checkers.inference.model.VariableSlot;
import checkers.inference.qual.VarAnnot;
import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTRecord;

/**
 * The default implementation of SlotManager.
 * @see checkers.inference.SlotManager
 */
public class DefaultSlotManager implements SlotManager {

    private final AnnotationMirror varAnnot;

    /**
     * The top annotation in the real qualifier hierarchy.
     * Currently, we assume there's only one top.
     */
    private final AnnotationMirror realTop;

    // This id starts at 1 because in some serializer's
    // (CnfSerializer) 0 is used as line delimiters.
    // Monotonically increasing id for all VariableSlots (including
    // subtypes of VariableSlots).
    private int nextId = 1;

    /**
     * A map for storing all the slots encountered by this slot manager. Key is
     * an {@link Integer}, representing a slot id. Value is a
     * {@link Slot} that corresponds to this slot id. Note that
     * ConstantSlots are also stored in this map, since ConstantSlot is subclass
     * of Slot.
     */
    private final Map<Integer, Slot> slots;

    /**
     * A map of {@link AnnotationMirror} to {@link Integer} for caching
     * ConstantSlot. Each {@link AnnotationMirror} uniquely identify a
     * ConstantSlot. {@link Integer} is the id of the corresponding ConstantSlot
     */
    private final Map<AnnotationMirror, Integer> constantCache;

    /**
     * A map of {@link AnnotationLocation} to {@link Integer} for caching
     * VariableSlot and RefinementVariableSlot. Those two kinds of slots can be
     * uniquely identified by their {@link AnnotationLocation}(Except MissingLocation).
     * {@link Integer} is the id of the corresponding VariableSlot or RefinementVariableSlot
     */
    private final Map<AnnotationLocation, Integer> locationCache;

    /**
     * A map of {@link Pair} of {@link Slot} to {@link Integer} for
     * caching ExistentialVariableSlot. Each ExistentialVariableSlot can be
     * uniquely identified by its potential and alternative VariablesSlots.
     * {@link Integer} is the id of the corresponding ExistentialVariableSlot
     */
    private final Map<Pair<Slot, Slot>, Integer> existentialSlotPairCache;

    /**
     * A map of {@link Pair} of {@link Slot} to {@link Integer} for caching
     * CombVariableSlot. Each combination of receiver slot and declared slot
     * uniquely identifies a CombVariableSlot. {@link Integer} is the id of the
     * corresponding CombVariableSlott
     */
    private final Map<Pair<Slot, Slot>, Integer> combSlotPairCache;
    private final Map<Pair<Slot, Slot>, Integer> lubSlotPairCache;
    private final Map<Pair<Slot, Slot>, Integer> glbSlotPairCache;

    /**
     * A map of tree to {@link AnnotationMirror} for caching
     * a set of pre-computed default types for the current compilation unit.
     */
    private final Map<Tree, AnnotationMirror> defaultAnnotationsCache;

    /**
     * A map of {@link AnnotationLocation} to {@link Integer} for caching
     * {@link ArithmeticVariableSlot}s. The annotation location uniquely identifies an
     * {@link ArithmeticVariableSlot}. The {@link Integer} is the Id of the corresponding
     * {@link ArithmeticVariableSlot}.
     */
    private final Map<AnnotationLocation, Integer> arithmeticSlotCache;

    /**
     * A map of {@link AnnotationLocation} to {@link Integer} for caching
     * {@link ComparisonVariableSlot}s. The annotation location uniquely identifies an
     * {@link ComparisonVariableSlot}. The {@link Integer} is the Id of the corresponding
     * {@link ComparisonVariableSlot}.
     */
    private final Map<AnnotationLocation, Integer> comparisonThenSlotCache;

    /**
     * A map of {@link AnnotationLocation} to {@link Integer} for caching
     * {@link ComparisonVariableSlot}s. The annotation location uniquely identifies an
     * {@link ComparisonVariableSlot}. The {@link Integer} is the Id of the corresponding
     * {@link ComparisonVariableSlot}.
     */
    private final Map<AnnotationLocation, Integer> comparisonElseSlotCache;

    private final Set<Class<? extends Annotation>> realQualifiers;
    private final ProcessingEnvironment processingEnvironment;

    public DefaultSlotManager( final ProcessingEnvironment processingEnvironment,
                               final AnnotationMirror realTop,
                               final Set<Class<? extends Annotation>> realQualifiers,
                               boolean storeConstants) {
        this.processingEnvironment = processingEnvironment;
        this.realTop = realTop;
        // sort the qualifiers so that they are always assigned the same varId
        this.realQualifiers = sortAnnotationClasses(realQualifiers);
        slots = new LinkedHashMap<>();

        AnnotationBuilder builder = new AnnotationBuilder(processingEnvironment, VarAnnot.class);
        builder.setValue("value", -1 );
        this.varAnnot = builder.build();

        // Construct empty caches
        constantCache = AnnotationUtils.createAnnotationMap();
        locationCache = new LinkedHashMap<>();
        existentialSlotPairCache = new LinkedHashMap<>();
        combSlotPairCache = new LinkedHashMap<>();
        lubSlotPairCache = new LinkedHashMap<>();
        glbSlotPairCache = new LinkedHashMap<>();
        arithmeticSlotCache = new LinkedHashMap<>();
        comparisonThenSlotCache = new LinkedHashMap<>();
        comparisonElseSlotCache = new LinkedHashMap<>();
        defaultAnnotationsCache = new LinkedHashMap<>();

        if (storeConstants) {
            for (Class<? extends Annotation> annoClass : this.realQualifiers) {
                AnnotationMirror am = new AnnotationBuilder(processingEnvironment, annoClass).build();
                ConstantSlot constantSlot = new ConstantSlot(nextId(), am);
                addToSlots(constantSlot);
                constantCache.put(am, constantSlot.getId());
            }
        }
    }
    private Set<Class<? extends Annotation>> sortAnnotationClasses(Set<Class<? extends Annotation>> annotations) {

        TreeSet<Class<? extends Annotation>> set = new TreeSet<>(new Comparator<Class<? extends Annotation>>() {
            @Override
            public int compare(Class<? extends Annotation> o1, Class<? extends Annotation> o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.getCanonicalName().compareTo(o2.getCanonicalName());
            }
        });
        set.addAll(annotations);
        return set;
    }

    /**
     * Returns the next unique variable id.  These id's are monotonically increasing.
     * @return the next variable id to be used in VariableCreation
     */
    private int nextId() {
        return nextId++;
    }

    private void addToSlots(final Slot slot) {
        slots.put(slot.getId(), slot);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Slot getSlot( int id ) {
        return slots.get(id);
    }

    /**
     * @inheritDoc
     */
    @Override
    public AnnotationMirror getAnnotation(final Slot slot) {
        // We need to build the AnnotationBuilder each time because AnnotationBuilders are only
        // allowed to build their annotations once
        return convertVariable(slot,
                new AnnotationBuilder(processingEnvironment, VarAnnot.class));
    }

    /**
     * Converts the given VariableSlot into an annotation using the given AnnotationBuiklder
     * @param variable VariableSlot to convert
     * @param annotationBuilder appropriate annotation for the actual class of the VariableSlot which could be subtype
     *                          of VariableSlot.  Eg.  CombVariableSlots use combVarBuilder which is parameterized to
     *                          build @CombVarAnnots
     * @return An annotation representing variable
     */
    private AnnotationMirror convertVariable( final Slot variable, final AnnotationBuilder annotationBuilder) {
        annotationBuilder.setValue("value", variable.getId() );
        return annotationBuilder.build();
    }

    // TODO: RENAME AND UPDATE DOCS
    /**
     * @inheritDoc
     */
    @Override
    public Slot getSlot( final AnnotatedTypeMirror atm ) {

        AnnotationMirror annot = atm.getAnnotationInHierarchy(this.varAnnot);
        if (annot == null) {
            if (InferenceMain.isHackMode()) {
                return null;
            }

            throw new BugInCF("Missing VarAnnot annotation: " + atm);
        }

        return getSlot(annot);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Slot getSlot( final AnnotationMirror annotationMirror ) {

        final int id;
        if (InferenceQualifierHierarchy.isVarAnnot(annotationMirror)) {
            if (annotationMirror.getElementValues().isEmpty()) {
                return null; // TODO: should we instead throw an exception?
            } else {
                final AnnotationValue annoValue = annotationMirror.getElementValues().values().iterator().next();
                id = Integer.valueOf( annoValue.toString() );
            }

            return getSlot( id );

        } else {

            if (constantCache.containsKey(annotationMirror)) {
                ConstantSlot constantSlot = (ConstantSlot) getSlot(
                        constantCache.get(annotationMirror));
                return constantSlot;

            } else {
                for (Class<? extends Annotation> realAnno : realQualifiers) {
                    if (InferenceMain.getInstance().getRealTypeFactory().areSameByClass(annotationMirror, realAnno)) {
                        return createConstantSlot(annotationMirror);
                    }
                }
            }
        }

        if (InferenceMain.isHackMode()) {
            return createConstantSlot(InferenceMain.getInstance().getRealTypeFactory().
                    getQualifierHierarchy().getTopAnnotations().iterator().next());
        }
        throw new BugInCF( annotationMirror + " is a type of AnnotationMirror not handled by getVariableSlot." );
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Slot> getSlots() {
        return new ArrayList<Slot>(this.slots.values());
    }

    // Sometimes, I miss scala.
    /**
     * @inheritDoc
     */
    @Override
    public List<VariableSlot> getVariableSlots() {
        List<VariableSlot> varSlots = new ArrayList<>();
        for (Slot slot : slots.values()) {
            if (slot instanceof VariableSlot) {
                varSlots.add((VariableSlot) slot);
            }
        }
        return varSlots;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<ConstantSlot> getConstantSlots() {
        List<ConstantSlot> constants = new ArrayList<>();
        for (Slot slot : slots.values()) {
            if (slot instanceof ConstantSlot) {
                constants.add((ConstantSlot) slot);
            }
        }
        return constants;
    }

    @Override
    public int getNumberOfSlots() {
        return nextId - 1;
    }

    @Override
    public SourceVariableSlot createSourceVariableSlot(AnnotationLocation location, TypeMirror type) {
        AnnotationMirror defaultAnnotation = null;
        if (!InferenceOptions.makeDefaultsExplicit) {
            // retrieve the default annotation when needed
            defaultAnnotation = getDefaultAnnotationForLocation(location, type);
        }

        SourceVariableSlot sourceVarSlot;
        if (location.getKind() == AnnotationLocation.Kind.MISSING) {
            if (InferenceMain.isHackMode()) {
                //Don't cache slot for MISSING LOCATION. Just create a new one and return.
                sourceVarSlot = new SourceVariableSlot(nextId(), location, type, defaultAnnotation, true);
                addToSlots(sourceVarSlot);
            } else {
                throw new BugInCF("Creating SourceVariableSlot on MISSING_LOCATION!");
            }

        } else if (locationCache.containsKey(location)) {
            int id = locationCache.get(location);
            sourceVarSlot = (SourceVariableSlot) getSlot(id);
        } else {
            sourceVarSlot = new SourceVariableSlot(nextId(), location, type, defaultAnnotation, true);
            addToSlots(sourceVarSlot);
            locationCache.put(location, sourceVarSlot.getId());
        }
        return sourceVarSlot;
    }

    /**
     * Find the default annotation for this location by checking the real type factory.
     * @param location location to create a new {@link SourceVariableSlot}
     * @return the default annotation for the given location
     */
    private @Nullable AnnotationMirror getDefaultAnnotationForLocation(AnnotationLocation location, TypeMirror type) {
        if (location == AnnotationLocation.MISSING_LOCATION) {
            if (InferenceMain.isHackMode()) {
                return null;
            } else {
                throw new BugInCF("Getting default annotation for missing location!");
            }
        }

        final Tree tree; // the tree associated with the location
        BaseAnnotatedTypeFactory realTypeFactory = InferenceMain.getInstance().getRealTypeFactory();

        if (location instanceof AnnotationLocation.AstPathLocation) {
            tree = getTreeForLocation((AnnotationLocation.AstPathLocation) location);
        } else if (location instanceof AnnotationLocation.ClassDeclLocation) {
            tree = getTreeForLocation(
                    (AnnotationLocation.ClassDeclLocation) location,
                    type,
                    realTypeFactory
            );
        } else {
            throw new BugInCF("Unable to find default annotation for location " + location);
        }

        if (!defaultAnnotationsCache.containsKey(tree)) {
            // If cache misses, we check if the top level class has changed.
            ClassTree topLevelClass = InferenceMain.getInstance().getVisitor().getCurrentTopLevelClass();

            if (!defaultAnnotationsCache.containsKey(topLevelClass)) {
                // If the top level has changed, we refresh our cache with the new scope.
                defaultAnnotationsCache.clear();

                Map<Tree, AnnotatedTypeMirror> defaultTypes = SlotDefaultTypeResolver.resolve(
                        topLevelClass,
                        realTypeFactory
                );

                // find default types in the current hierarchy and save them to the cache
                for (Map.Entry<Tree, AnnotatedTypeMirror> entry : defaultTypes.entrySet()) {
                    defaultAnnotationsCache.put(
                            entry.getKey(),
                            entry.getValue().getAnnotationInHierarchy(this.realTop)
                    );
                }
            }
        }

        AnnotationMirror realAnnotation = defaultAnnotationsCache.get(tree);
        if (tree != null && realAnnotation == null) {
            // If its default type can't be found in the cache, we can
            // fallback to the simplest method.
            realAnnotation = realTypeFactory.getAnnotatedType(tree).getAnnotationInHierarchy(this.realTop);
        }
        return realAnnotation;
    }

    /**
     * Find the tree associated with the given {@link AnnotationLocation.AstPathLocation}.
     * @param location location to find the tree
     * @return the tree associated with the given location, which can be null if the location
     *      is not under the current compilation unit
     */
    private @Nullable Tree getTreeForLocation(AnnotationLocation.AstPathLocation location) {
        ASTRecord astRecord = location.getAstRecord();
        CompilationUnitTree root = astRecord.ast;
        return ASTIndex.getNode(root, astRecord);
    }

    /**
     * Find the class tree associated with the given {@link AnnotationLocation.ClassDeclLocation}.
     * @param realTypeFactory the current real {@link BaseAnnotatedTypeFactory}
     * @param location location to find the tree
     * @param type type of the associated class
     * @return the class tree associated with the given location
     */
    private Tree getTreeForLocation(
            AnnotationLocation.ClassDeclLocation location,
            TypeMirror type,
            BaseAnnotatedTypeFactory realTypeFactory
    ) {
        Element element = processingEnvironment.getTypeUtils().asElement(type);
        if (!(element instanceof TypeElement)) {
            throw new BugInCF(
                    "Expected to get a TypeElement for %s at %s, but received %s.", type, location, element);
        }

        TypeElement typeElement = (TypeElement) element;
        Name fullyQualifiedName = ((Symbol.ClassSymbol)typeElement).flatName();
        if (!fullyQualifiedName.contentEquals(location.getFullyQualifiedClassName())) {
            throw new BugInCF(
                    "TypeElement for %s has qualified name %s, and it doesn't match with the location %s",
                    type, fullyQualifiedName, location);
        }

        return realTypeFactory.declarationFromElement(typeElement);
    }

    @Override
    public RefinementVariableSlot createRefinementVariableSlot(AnnotationLocation location, Slot declarationSlot, Slot valueSlot) {
        // If the location is already cached, return the corresponding refinement slot in the cache
        if (locationCache.containsKey(location)) {
            int id = locationCache.get(location);
            return (RefinementVariableSlot) getSlot(id);
        }

        // Create new refinement variable slot, as well as the equality constraint to the value slot
        RefinementVariableSlot refinementVariableSlot;
        refinementVariableSlot = new RefinementVariableSlot(nextId(), location, declarationSlot);
        addToSlots(refinementVariableSlot);
        if (valueSlot != null) {
            // If the rhs value slot passed in is non-null, create the equality constraint on it
            InferenceMain.getInstance().getConstraintManager().addEqualityConstraint(refinementVariableSlot, valueSlot);
        }

        // Only cache slot for non-MISSING LOCATION
        // TODO: We should always create refinement variable on a non-missing location,
        //  and remove this if-condition
        if (location.getKind() != AnnotationLocation.Kind.MISSING) {
            locationCache.put(location, refinementVariableSlot.getId());
        }
        return refinementVariableSlot;
    }

    @Override
    public ConstantSlot createConstantSlot(AnnotationMirror value) {
        ConstantSlot constantSlot;
        if (constantCache.containsKey(value)) {
            int id = constantCache.get(value);
            constantSlot = (ConstantSlot) getSlot(id);
        } else {
            constantSlot = new ConstantSlot(nextId(), value);
            addToSlots(constantSlot);
            constantCache.put(value, constantSlot.getId());
        }
        return constantSlot;
    }

    @Override
    public CombVariableSlot createCombVariableSlot(Slot receiver, Slot declared) {
        CombVariableSlot combVariableSlot;
        Pair<Slot, Slot> pair = new Pair<>(receiver, declared);
        if (combSlotPairCache.containsKey(pair)) {
            int id = combSlotPairCache.get(pair);
            combVariableSlot = (CombVariableSlot) getSlot(id);
        } else {
            combVariableSlot = new CombVariableSlot(nextId(), null, receiver, declared);
            addToSlots(combVariableSlot);
            combSlotPairCache.put(pair, combVariableSlot.getId());
        }
        return combVariableSlot;
    }

    @Override
    public LubVariableSlot createLubMergeVariableSlot(Slot left, Slot right) {
        return createMergeVariableSlot(left, right, true);
    }

    @Override
    public LubVariableSlot createGlbMergeVariableSlot(Slot left, Slot right) {
        return createMergeVariableSlot(left, right, false);
    }

    private LubVariableSlot createMergeVariableSlot(Slot left, Slot right, boolean isLub) {
        // Order of two ingredient slots doesn't matter, but for simplicity, we still use pair.
        LubVariableSlot mergeVariableSlot;
        Map<Pair<Slot, Slot>, Integer> cache = isLub ? lubSlotPairCache : glbSlotPairCache;
        Pair<Slot, Slot> pair = new Pair<>(left, right);

        if (cache.containsKey(pair)) {
            int id = cache.get(pair);
            mergeVariableSlot = (LubVariableSlot) getSlot(id);
        } else {
            // We need a non-null location in the future for better debugging outputs
            mergeVariableSlot = new LubVariableSlot(nextId(), null, left, right);
            addToSlots(mergeVariableSlot);
            cache.put(pair, mergeVariableSlot.getId());
        }
        return mergeVariableSlot;
    }

    @Override
    public ExistentialVariableSlot createExistentialVariableSlot(Slot potentialSlot, Slot alternativeSlot) {
        ExistentialVariableSlot existentialVariableSlot;
        Pair<Slot, Slot> pair = new Pair<>(potentialSlot, alternativeSlot);
        if (existentialSlotPairCache.containsKey(pair)) {
            int id = existentialSlotPairCache.get(pair);
            existentialVariableSlot = (ExistentialVariableSlot) getSlot(id);
        } else {
            existentialVariableSlot = new ExistentialVariableSlot(nextId(), potentialSlot, alternativeSlot);
            addToSlots(existentialVariableSlot);
            existentialSlotPairCache.put(pair, existentialVariableSlot.getId());
        }
        return existentialVariableSlot;
    }

    /**
     *  Determine the type kind of an arithmetic operation, based on Binary Numeric Promotion in JLS 5.6.2.
     * @param lhsAtm atm of left operand
     * @param rhsAtm atm of right operand
     * @return type kind of the arithmetic operation
     */
    private TypeKind getArithmeticResultKind(AnnotatedTypeMirror lhsAtm, AnnotatedTypeMirror rhsAtm) {
        TypeMirror lhsType = lhsAtm.getUnderlyingType();
        TypeMirror rhsType = rhsAtm.getUnderlyingType();

        assert (TypesUtils.isPrimitiveOrBoxed(lhsType) && TypesUtils.isPrimitiveOrBoxed(rhsType));

        if (TypesUtils.isFloatingPoint(lhsType) || TypesUtils.isFloatingPoint(rhsType)) {
            return TypeKind.DOUBLE;
        }

        if (TypeKindUtils.primitiveOrBoxedToTypeKind(lhsType) == TypeKind.LONG
                || TypeKindUtils.primitiveOrBoxedToTypeKind(rhsType) == TypeKind.LONG) {
            return TypeKind.LONG;
        }

        return TypeKind.INT;
    }

    @Override
    public ArithmeticVariableSlot createArithmeticVariableSlot(
            AnnotationLocation location, AnnotatedTypeMirror lhsAtm, AnnotatedTypeMirror rhsAtm) {
        if (location == null || location.getKind() == AnnotationLocation.Kind.MISSING) {
            throw new BugInCF(
                    "Cannot create an ArithmeticVariableSlot with a missing annotation location.");
        }

        if (arithmeticSlotCache.containsKey(location)) {
            return (ArithmeticVariableSlot) getSlot(arithmeticSlotCache.get(location));
        }

        // create the arithmetic var slot if it doesn't exist for the given location
        TypeKind kind = getArithmeticResultKind(lhsAtm, rhsAtm);
        ArithmeticVariableSlot slot = new ArithmeticVariableSlot(nextId(), location, kind);
        addToSlots(slot);
        arithmeticSlotCache.put(location, slot.getId());
        return slot;
    }

    @Override
    public ComparisonVariableSlot createComparisonVariableSlot(AnnotationLocation location, Slot refined, boolean thenBranch) {
        if (location == null || location.getKind() == AnnotationLocation.Kind.MISSING) {
            throw new BugInCF(
                    "Cannot create an ComparisonVariableSlot with a missing annotation location.");
        }

        if (thenBranch && comparisonThenSlotCache.containsKey(location)) {
            return (ComparisonVariableSlot) getSlot(comparisonThenSlotCache.get(location));
        }
        if (!thenBranch && comparisonElseSlotCache.containsKey(location)) {
            return (ComparisonVariableSlot) getSlot(comparisonElseSlotCache.get(location));
        }

        // create the comparison var slot if it doesn't exist for the given location
        ComparisonVariableSlot slot = new ComparisonVariableSlot(nextId(), location, refined);
        addToSlots(slot);
        if (thenBranch) {
            comparisonThenSlotCache.put(location, slot.getId());
        } else {
            comparisonElseSlotCache.put(location, slot.getId());
        }
        return slot;
    }

    @Override
    public AnnotationMirror createEquivalentVarAnno(AnnotationMirror realQualifier) {
        ConstantSlot varSlot = createConstantSlot(realQualifier);
        return getAnnotation(varSlot);
    }
}
