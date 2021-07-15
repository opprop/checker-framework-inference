import ostrusted.qual.OsUntrusted;
import ostrusted.qual.OsTrusted;

/**
 * This case is unsatisfiable, because in method {@code m()},
 * <p>(1) at line 18, the local variable {@code s} is refined by the return type of {@code bar()}.
 *     Denote the refinement variable as @1, then @1 == @OsUntrusted.
 * <p>(2) at line 20, {@code s} is passed to method {@code foo} as the argument, therefore @1 <: @OsTrusted
 *     since @OsTrusted is bottom, @1 == @OsTrusted.
 */
class Unsat {

    void foo(@OsTrusted String s) {}

    @OsUntrusted String bar() { return ""; }

    void m() {
        String s = bar();
        // :: error: (argument.type.incompatible)
        foo(s);
    }
}
