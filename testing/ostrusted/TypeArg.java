import java.util.*;
import ostrusted.qual.*;

public class TypeArg {
    // t1 - t3 tests type argument annotations
    void t1(List<@OsTrusted String> li, String s) {
        // :: fixable-error: (assignment.type.incompatible)
        li.add(s);
    }

    void t2() {
        // :: fixable-error: (assignment.type.incompatible)
        List<String> li = new ArrayList<@OsTrusted String>();
    }

    void t3(List<String> ss) {
        // :: fixable-error: (assignment.type.incompatible)
        List<String> li = new ArrayList<@OsTrusted String>(ss);
    }

    // t4 - t5 tests primary annotations
    @OsTrusted
    List<String> t4() {
        List<String> li = new ArrayList<String>();
        // :: fixable-error: (assignment.type.incompatible)
        return li;
    }

    void t5(List<String> ss) {
        // :: fixable-error: (assignment.type.incompatible)
        List<String> li = new @OsTrusted ArrayList<String>(ss);
    }
}
