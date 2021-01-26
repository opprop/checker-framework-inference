import refval.qual.RefVal;

public class TestStringInvalid {

    // :: error: (assignment.type.incompatible)
    @RefVal(typeNames = {"java.lang.Object"}) String invalidString = "I am a String!";
}
