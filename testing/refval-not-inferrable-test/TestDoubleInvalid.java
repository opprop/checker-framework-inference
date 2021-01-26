import refval.qual.RefVal;

public class TestDoubleInvalid {

    // :: error: (assignment.type.incompatible)
    @RefVal(typeNames = {"int"}) double invalidDouble = 3.14;
}
