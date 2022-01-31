package sparta.checkers.iflow.util;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import org.checkerframework.javacutil.TreeUtils;
import sparta.checkers.qual.FlowPermission;
import sparta.checkers.qual.PolyFlow;
import sparta.checkers.qual.PolySink;
import sparta.checkers.qual.PolySource;
import sparta.checkers.qual.Sink;
import sparta.checkers.qual.Source;

public class IFlowUtils {
    private static PFPermission ANY = new PFPermission(FlowPermission.ANY);

    private static final String SINK_NAME = Sink.class.getCanonicalName();
    private static final String SOURCE_NAME = Source.class.getCanonicalName();
    private static final String POLYSINK_NAME = PolySink.class.getCanonicalName();
    private static final String POLYSOURCE_NAME = PolySource.class.getCanonicalName();

    /** The Sink.value element/field. */
    private final ExecutableElement sinkValueElement;

    /** The Source.value element/field. */
    private final ExecutableElement sourceValueElement;

    public IFlowUtils(ProcessingEnvironment processingEnv) {
        sinkValueElement = TreeUtils.getMethod(Sink.class, "value", processingEnv);
        sourceValueElement = TreeUtils.getMethod(Source.class, "value", processingEnv);
    }

    public Set<PFPermission> getSinks(final AnnotatedTypeMirror type) {
        for (AnnotationMirror anno : type.getEffectiveAnnotations()) {
            if (isSink(anno)) {
                return getSinks(anno);
            }
        }
        return new TreeSet<PFPermission>();
    }

    public Set<PFPermission> getSources(final AnnotatedTypeMirror type) {
        for (AnnotationMirror anno : type.getEffectiveAnnotations()) {
            if (isSource(anno)) {
                return getSources(anno);
            }
        }
        return new TreeSet<PFPermission>();
    }

    public Set<PFPermission> getSinks(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<PFPermission>();
        }

        List<String> sinks = getRawSinks(am);
        Set<PFPermission> sinkFlowPermissions = new TreeSet<PFPermission>();
        for (String permissionString : sinks) {
            sinkFlowPermissions.add(PFPermission.convertStringToPFPermission(permissionString));
        }

        return convertToAnySink(sinkFlowPermissions, false);
    }

    public List<String> getRawSinks(final AnnotationMirror am) {
        return AnnotationUtils.getElementValueArray(am, sinkValueElement, String.class, Collections.emptyList());
    }

    public Set<PFPermission> getSources(final AnnotationMirror am) {
        if (am == null) {
            return new TreeSet<PFPermission>();
        }

        List<String> sources = getRawSources(am);
        Set<PFPermission> sourceFlowPermissions = new TreeSet<PFPermission>();
        for (String permissionString : sources) {
            sourceFlowPermissions.add(PFPermission.convertStringToPFPermission(permissionString));
        }

        return convertToAnySource(sourceFlowPermissions, false);
    }

    public List<String> getRawSources(final AnnotationMirror am) {
        return AnnotationUtils.getElementValueArray(am, sourceValueElement, String.class, Collections.emptyList());
    }

    /**
     * Replace ANY with the list of all possible sinks
     * @param sinks
     * @param inPlace
     * @return
     */
    private static Set<PFPermission> convertAnyToAllSinks(final Set<PFPermission> sinks,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sinks : new TreeSet<PFPermission>(sinks);
        if (sinks.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }

    /**
     * Replace ANY with the list of all possible sources
     * @param sources
     * @param inPlace
     * @return
     */
    private static Set<PFPermission> convertAnytoAllSources(final Set<PFPermission> sources,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sources : new TreeSet<PFPermission>(
                sources);
        if (sources.contains(ANY)) {
            retSet.addAll(getSetOfAllSinks());
            retSet.remove(ANY);
        }
        return retSet;
    }

    /**
     * All possible sources, excluding ANY
     * TODO: This returns all sources and sinks, not just sources...fix this
     * @return
     */
    public static Set<PFPermission> getSetOfAllSources() {
        if (setOfAllSources.isEmpty()) {
            List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission.values());
            for (FlowPermission permission : coarseFlowList) {
                if (permission != FlowPermission.ANY) {
                    setOfAllSources.add(new PFPermission(permission));
                }
            }
        }
        return setOfAllSources;
    }
    static Set<PFPermission> setOfAllSources = new TreeSet<>();

    /**
     * All possible sinks, excluding ANY
     * TODO: This returns all sources and sinks, not just sinks...fix this
     * @return
     */
    public static Set<PFPermission> getSetOfAllSinks() {
        if (setOfAllSinks.isEmpty()) {
            List<FlowPermission> coarseFlowList = Arrays.asList(FlowPermission
                    .values());
            for (FlowPermission permission : coarseFlowList) {
                if (permission != FlowPermission.ANY) {
                    setOfAllSinks.add(new PFPermission(permission));
                }
            }
        }
        return setOfAllSinks;
    }

    static Set<PFPermission> setOfAllSinks = new TreeSet<>();


    /**
     * If sources contains all possible sources, then return ANY.
     * If sources contains ANY and some other sources, then return ANY
     * @param sources
     * @param inPlace
     * @return
     */
    public static Set<PFPermission> convertToAnySource(final Set<PFPermission> sources,
            boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sources : new TreeSet<PFPermission>(sources);
        if (retSet.equals(getSetOfAllSources())) {
            retSet.clear();
            retSet.add(ANY);
        } else if (retSet.contains(ANY)) {
            retSet.clear();
            retSet.add(ANY);
        }
        return retSet;
    }

    /**
     * If sinks contains all possible sinks, then return ANY.
     * If sinks contains ANY and some other sinks, then return ANY
     * @param sinks
     * @param inPlace
     * @return either {ANY} or sinks
     */
    public static Set<PFPermission> convertToAnySink(final Set<PFPermission> sinks, boolean inPlace) {
        final Set<PFPermission> retSet = (inPlace) ? sinks : new TreeSet<PFPermission>(sinks);
        if(sinks.equals(getSetOfAllSinks())) {
            retSet.clear();
            retSet.add(ANY);
        } else if (retSet.contains(ANY)) {
            retSet.clear();
            retSet.add(ANY);
        }
        return retSet;
    }

    public boolean isTop(AnnotatedTypeMirror atm) {
        Set<PFPermission> sources = getSources(atm);
        Set<PFPermission> sinks = getSinks(atm);
        return sources.contains(ANY) && sinks.isEmpty();
    }

    public boolean isBottom(AnnotatedTypeMirror atm) {
        Set<PFPermission> sources = getSources(atm);
        Set<PFPermission> sinks = getSinks(atm);
        return sinks.contains(ANY) && sources.isEmpty();
    }
    /**
     * Return the set of sources that both annotations have.
     * If the intersection is all possible sources, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public Set<PFPermission> intersectSources(AnnotationMirror a1, AnnotationMirror a2) {
        final Set<PFPermission> a1Set = getSources(a1);
        final Set<PFPermission> a2Set = getSources(a2);
        return intersectSources(a1Set, a2Set);

    }
    public static Set<PFPermission> intersectSources(Set<PFPermission> a1Set,
            Set<PFPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
       Set<PFPermission> retSet = new TreeSet<PFPermission>();
       Set<PFPermission> a1All =  convertAnytoAllSources(a1Set, false);
       Set<PFPermission> a2All =   convertAnytoAllSources(a2Set, false);
       for (PFPermission a1permission : a1All) {
           for (PFPermission a2permission : a2All) {
               // Match permission and match all parameters such that a2 is subsumed in a1
               if (a1permission.getPermission() == a2permission.getPermission() &&
                  allParametersMatch(a1permission.getParameters(), a2permission.getParameters())) {
                   retSet.add(a2permission);
               }
           }
       }
        return  convertToAnySource(retSet, false);
    }



    /**
     * Return the set of sinks that both annotations have.
     * If the intersection is all possible sinks, {ANY} is returned
     * @param a1 AnnotationMirror, could be {ANY}
     * @param a2 AnnotationMirror, could be {ANY}
     * @return intersection of a1 and a2
     */
    public Set<PFPermission> intersectSinks(AnnotationMirror a1, AnnotationMirror a2){
        final Set<PFPermission> a1Set = getSinks(a1);
        final Set<PFPermission> a2Set = getSinks(a2);
        return intersectSinks(a1Set, a2Set);
    }
    public static Set<PFPermission> intersectSinks(Set<PFPermission> a1Set,
            Set<PFPermission> a2Set) {
        if(a1Set == null || a2Set == null) return new TreeSet<>();
        Set<PFPermission> retSet = new TreeSet<PFPermission>();
        a1Set = convertAnyToAllSinks(a1Set, false);
        a2Set = convertAnyToAllSinks(a2Set, false);
        for (PFPermission a1permission : a1Set) {
            for (PFPermission a2permission : a2Set) {
                // Match permission and match all parameters such that a2 is subsumed in a1
                if (a1permission.getPermission() == a2permission.getPermission() &&
                    allParametersMatch(a2permission.getParameters(), a1permission.getParameters())) {
                    retSet.add(a2permission);
                }
            }
        }
        return convertToAnySink(retSet, false);
    }

    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public Set<PFPermission> unionSources(AnnotationMirror a1, AnnotationMirror a2){
        return unionSources(getSources(a1), getSources(a2));
    }
    public static Set<PFPermission> unionSources(Set<PFPermission> a1, Set<PFPermission> a2){
        a1.addAll(a2);
        convertToAnySource(a1, true);
        return a1;
    }


    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public Set<PFPermission> unionSinks(AnnotationMirror a1, AnnotationMirror a2){
        return unionSinks(getSinks(a1), getSinks(a2));
    }
    /**
     * Returns the union of a1 and a2.
     * If the union is {ANY, ...} then just {ANY} is returned
     * @param a1
     * @param a2
     * @return
     */
    public static Set<PFPermission> unionSinks(Set<PFPermission> a1, Set<PFPermission> a2){
        a1.addAll(a2);
        convertToAnySink(a1, true);
        return a1;
    }

    public static Set<PFPermission> convertToParameterizedFlowPermission(Set<FlowPermission> permissions) {
        Set<PFPermission> flowPermissions = new TreeSet<PFPermission>();
        for (FlowPermission p : permissions) {
            flowPermissions.add(new PFPermission(p));
        }
        return flowPermissions;
    }

    public static Set<FlowPermission> convertFromParameterizedFlowPermission(Set<PFPermission> permissions) {
        Set<FlowPermission> coarsePermissions = new TreeSet<FlowPermission>();
        for (PFPermission p : permissions) {
            coarsePermissions.add(p.getPermission());
        }
        return coarsePermissions;
    }

    public static boolean isMatchInSet(PFPermission flowToMatch, Set<PFPermission> flows) {
        for (PFPermission flow : flows) {
            if (flowToMatch.getPermission() == flow.getPermission()) {
                if (allParametersMatch(flowToMatch.getParameters(), flow.getParameters())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean allParametersMatch(List<String> childParams, List<String> parentParams) {
        for (String currChildParam : childParams) {
            if (!singleParametersMatch(currChildParam, parentParams)) {
                return false;
            }
        }
        return true;
    }

    public static boolean singleParametersMatch(String param, List<String> parameters) {
        for (String currParam : parameters) {
            if (wildcardMatch(param, currParam)) {
                return true;
            }
        }
        return false;
    }

    public static boolean wildcardMatch(String child, String parent) {
        String regex = parent.replaceAll("\\*", "(.*)");
        return child.matches(regex);
    }

    public static AnnotationMirror createAnnoFromSink(final Set<PFPermission> sinks,
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Sink.class);
        return createIFlowAnnotation(sinks, builder);
    }

    public static AnnotationMirror createAnnoFromSource(Set<PFPermission> sources,
            ProcessingEnvironment processingEnv) {
        final AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
                Source.class);
        return createIFlowAnnotation(sources, builder);
    }

    private static AnnotationMirror createIFlowAnnotation(
            final Set<PFPermission> permObjects, final AnnotationBuilder builder) {
        List<String> permStrings = new ArrayList<>();
        for (PFPermission p : permObjects) {
            permStrings.add(p.toString());
        }
        builder.setValue("value", permStrings);
        return builder.build();
    }

    public static boolean isSink(AnnotationMirror am) {
        return AnnotationUtils.areSameByName(am, SINK_NAME);
    }

    public static boolean isPolySink(AnnotationMirror am) {
        return AnnotationUtils.areSameByName(am, POLYSINK_NAME);
    }

    public static boolean isSource(AnnotationMirror am) {
        return AnnotationUtils.areSameByName(am, SOURCE_NAME);
    }

    public static boolean isPolySource(AnnotationMirror am) {
        return AnnotationUtils.areSameByName(am, POLYSOURCE_NAME);
    }
}
