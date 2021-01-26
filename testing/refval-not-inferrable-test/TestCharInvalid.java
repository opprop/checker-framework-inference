import refval.qual.RefVal;

public class TestCharInvalid {

    // :: error: (assignment.type.incompatible)
    @RefVal(typeNames = {"int"}) char invalidChar = 'L';
}
