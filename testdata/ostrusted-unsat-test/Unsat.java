import ostrusted.qual.OsUntrusted;
import ostrusted.qual.OsTrusted;

class Unsat {

    void foo(@OsTrusted String s) {}

    @OsUntrusted
    String bar() { return ""; }

    void m() {
        String s = bar();
        // :: error: (argument.type.incompatible)
        foo(s);
    }
}
