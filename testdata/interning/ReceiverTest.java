import interning.qual.Interned;

public class ReceiverTest {
    ReceiverTest foo;
    void myMethod (@Interned ReceiverTest this) {
    }

    void otherMethod() {
        // :: error: (method.invocation.invalid)
        foo.myMethod ();
    }
}