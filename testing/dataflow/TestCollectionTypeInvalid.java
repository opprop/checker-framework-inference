import java.util.ArrayList;

import dataflow.qual.DataFlow;

public class TestCollectionTypeInvalid {
    // :: error: (assignment.type.incompatible)
    @DataFlow(typeNames = { "java.util.ArrayList<Object>" }) ArrayList collectionTypeTesing_invalid = new ArrayList<String>();
}
