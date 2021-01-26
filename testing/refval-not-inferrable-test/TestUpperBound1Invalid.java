import refval.qual.RefVal;

public class TestUpperBound1Invalid {

    public @RefVal(typeNames = {"float"})
    int invalidUpperBound(int c) {
        // :: error: (return.type.incompatible)
        return 3;
    }
}
