import dataflow.qual.DataFlow;

public class TestDoubleInvalid {

    // :: error: (assignment.type.incompatible)
    @DataFlow(typeNames = {"int"}) double invalidDouble = 3.14;
}
