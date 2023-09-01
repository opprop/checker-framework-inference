import java.util.ArrayList;

import dataflow.qual.DataFlow;

public class TestNewInvalid {

    // :: error: (assignment.type.incompatible)
    @DataFlow(typeNames = {"java.util.List"})
    ArrayList invalidNew = new ArrayList();
}
