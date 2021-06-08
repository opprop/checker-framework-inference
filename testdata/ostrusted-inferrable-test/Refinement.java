import ostrusted.qual.OsTrusted;

public class Refinement {

    void foo(Object in) {
        Object o = in;
        
        // :: fixable-error: (argument.type.incompatible) 
        bar(o);
    }

    void bar(@OsTrusted Object in) {}
}

