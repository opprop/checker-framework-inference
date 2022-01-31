import dataflow.qual.DataFlow;

public class TestStringInvalid {

    // :: error: (assignment.type.incompatible)
    @DataFlow(typeNames = {"java.lang.Object"}) String invalidString = "I am a String!";
}
