import ostrusted.qual.OsUntrusted;

class Anonymous {
    A a1 = new @OsUntrusted A() {};
}


class A {}



