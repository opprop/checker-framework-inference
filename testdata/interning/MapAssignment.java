import java.util.Map;

import interning.qual.Interned;

public class MapAssignment {

    Map<String, @Interned String> a;
    Map<@Interned String, String> b;

    void maps() {
        // :: fixable-error: (assignment.type.incompatible)
        a = b;
    }
}
