import ostrusted.qual.OsTrusted;
import ostrusted.qual.OsUntrusted;

public class Comparison {

    void foo(@OsUntrusted Object o) {
        if (o == null) {
            // Literal null is @OsTrusted. `o` is refined to @OsTrusted through ComparisonConstraint.
            bar(o);
        } 
    }

    @OsTrusted
    Object bar(Object o) {
        // :: fixable-error: (return.type.incompatible)
        return o;
    }
    
}

