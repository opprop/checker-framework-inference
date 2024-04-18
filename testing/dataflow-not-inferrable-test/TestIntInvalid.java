import dataflow.qual.DataFlow;

public class TestIntInvalid {

    // :: error: (assignment.type.incompatible)
    @DataFlow(typeNames = {"float"}) int invalidInteger = 3;
}
