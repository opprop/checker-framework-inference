import refval.qual.RefVal;

public class TestUpperBound2Invalid {

    public @RefVal(typeNames = {"java.lang.Object"})
    Object invalidUpperBound(int c) {
        // :: error: (return.type.incompatible)
        return "I am a String!";
    }
}
