package checkers.inference.solver.frontend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationUtils;

/**
 * Lattice class contains necessary information about qualifier hierarchy for
 * constraint constraint solving.
 * 
 * @author jianchu
 *
 */
public class Lattice {

    private QualifierHierarchy qualHierarchy;
    public Map<AnnotationMirror, Collection<AnnotationMirror>> subType = AnnotationUtils .createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> superType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Collection<AnnotationMirror>> incomparableType = AnnotationUtils.createAnnotationMap();
    public Map<AnnotationMirror, Integer> typeToInt = AnnotationUtils.createAnnotationMap();
    public Map<Integer, AnnotationMirror> intToType = new HashMap<Integer, AnnotationMirror>();
    private Set<? extends AnnotationMirror> allTypes;
    public AnnotationMirror top;
    public AnnotationMirror bottom;
    public int numTypes;

    public void configure() {
        allTypes = qualHierarchy.getTypeQualifiers();
        top = qualHierarchy.getTopAnnotations().iterator().next();
        bottom = qualHierarchy.getBottomAnnotations().iterator().next();
        numTypes = qualHierarchy.getTypeQualifiers().size();
        calculateSubSupertypes();
        calculateIncomparableTypes();
    }

    public Lattice() {
    }

    public Lattice(QualifierHierarchy qualHierarchy) {
        this.qualHierarchy = qualHierarchy;
    }

    /**
     * For each type qualifier, map it to a list of it's super types and
     * subtypes in two maps.
     */
    protected void calculateSubSupertypes() {
        int num = 0;
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> subtypeOfi = new HashSet<AnnotationMirror>();
            Set<AnnotationMirror> supertypeOfi = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (qualHierarchy.isSubtype(j, i)) {
                    subtypeOfi.add(j);
                }
                if (qualHierarchy.isSubtype(i, j)) {
                    supertypeOfi.add(j);
                }
            }
            subType.put(i, subtypeOfi);
            superType.put(i, supertypeOfi);
            typeToInt.put(i, num);
            intToType.put(num, i);
            num++;
        }
    }

    /**
     * For each type qualifier, map it to a list of it's incomparable types.
     */
    protected void calculateIncomparableTypes() {
        for (AnnotationMirror i : allTypes) {
            Set<AnnotationMirror> incomparableOfi = new HashSet<AnnotationMirror>();
            for (AnnotationMirror j : allTypes) {
                if (!subType.get(i).contains(j) && !subType.get(j).contains(i)) {
                    incomparableOfi.add(j);
                }
            }
            if (!incomparableOfi.isEmpty()) {
                incomparableType.put(i, incomparableOfi);
            }
        }
    }

    public Set<? extends AnnotationMirror> getAllTypes() {
        return this.allTypes;
    }

}
