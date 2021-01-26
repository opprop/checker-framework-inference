import refval.qual.RefVal;

public class TestUpperBound3Invalid {

    public @RefVal(typeNames = {"float", "java.lang.String"})
    Object invalidUpperBound(int c) {
        if (c > 0) {
            // :: error: (return.type.incompatible)
            return 3;
        }
        return "I am a String!";
    }
}
