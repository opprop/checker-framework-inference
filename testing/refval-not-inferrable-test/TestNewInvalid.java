import refval.qual.RefVal;

import java.util.ArrayList;

public class TestNewInvalid {

    // :: error: (assignment.type.incompatible)
    @RefVal(typeNames = {"java.util.List"}) ArrayList invalidNew = new ArrayList();
}
