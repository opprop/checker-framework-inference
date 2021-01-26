import refval.qual.RefVal;

public class TestIntInvalid {

    // :: error: (assignment.type.incompatible)
    @RefVal(typeNames = {"float"}) int invalidInteger = 3;
}
