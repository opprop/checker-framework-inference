import java.util.ArrayList;

import dataflow.qual.RefVal;

public class TestCollectionTypeInvalid {

    @RefVal(typeNames = {"java.util.ArrayList<Object>"})
    // :: error: (assignment.type.incompatible)
    ArrayList invalidCollection = new ArrayList<String>();
}
