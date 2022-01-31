import dataflow.qual.DataFlow;

public class TestUpperBound2Invalid {

    public @DataFlow(typeNames = {"java.lang.Object"})
    Object invalidUpperBound(int c) {
        // :: error: (return.type.incompatible)
        return "I am a String!";
    }
}
