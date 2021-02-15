import java.util.*;
import ostrusted.qual.*;

public class TypeArgSAT {
    void t1(List<@OsTrusted String> li, String s) {
        // :: fixable-error: (assignment.type.incompatible)
        li.add(s);
    }

    void t2(List<@OsTrusted String> li, @OsUntrusted String s) {
        // The following commented line will cause no solution for inference
        // :: error: (assignment.type.incompatible)
        li.add(s);
    }

    void t3(String s) {
        List<String> li = new ArrayList<String>();
        li.add(s);
    }
}
