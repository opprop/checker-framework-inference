import ostrusted.qual.OsTrusted;
import ostrusted.qual.OsUntrusted;

public class Comparison {

    void foo(@OsUntrusted Object o) {
        if (o == null) {
            bar(o);
        } 
    }

    @OsTrusted
    Object bar(Object o) {
        // :: fixable-error: (return.type.incompatible)
        return o;
    }
    
}

