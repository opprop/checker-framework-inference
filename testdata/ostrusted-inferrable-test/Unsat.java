import ostrusted.qual.OsUntrusted;
import ostrusted.qual.OsTrusted;

class Unsat {

    @OsUntrusted String bar() { return ""; }

    void m() {
        // :: error: (assignment.type.incompatible)
        @OsTrusted String s = bar();
    }
}
