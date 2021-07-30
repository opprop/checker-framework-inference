import ostrusted.qual.OsTrusted;
import ostrusted.qual.OsUntrusted;

class Anonymous {

    @OsTrusted A foo() {
        A a1 = new @OsUntrusted A() {};
        A a2 = a1;
        // :: error: (return.type.incompatible)
        return a2;
    }
}

class A {}



