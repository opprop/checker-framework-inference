import java.util.ArrayList;

import refval.qual.RefVal;

public class TestCollectionTypeInvalid {

    @RefVal(typeNames = {"java.util.ArrayList<Object>"})
    // :: error: (assignment.type.incompatible)
    ArrayList invalidCollection = new ArrayList<String>();
}
