import java.util.ArrayList;

import dataflow.qual.DataFlow;

public class TestCollectionTypeInvalid {

    @DataFlow(typeNames = {"java.util.ArrayList<Object>"})
    // :: error: (assignment.type.incompatible)
    ArrayList invalidCollection = new ArrayList<String>();
}
