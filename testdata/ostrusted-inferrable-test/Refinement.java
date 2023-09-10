import ostrusted.qual.OsUntrusted;
import ostrusted.qual.OsTrusted;

public class Refinement {

    void foo(Object in1, Object in2) {
        Object o = in1;
        // :: fixable-error: (argument.type.incompatible)
        bar(o);

        o = in2;
        // :: fixable-error: (argument.type.incompatible)
        bar(o);
    }

    void bar(@OsTrusted Object in) {}


    @OsTrusted Object m(@OsUntrusted Object untrusted, Object trusted) {
        Object obj = untrusted;
        obj = trusted;
        // :: fixable-error: (return.type.incompatible)
        return obj;
    }
}

