import ostrusted.qual.OsTrusted;
import ostrusted.qual.OsUntrusted;
import java.nio.file.SimpleFileVisitor;

public class AnonymousProblem {

    // The constructor return type of the RHS anonymous class is possibly inferred as
    // @OsTrusted, since we don't enforce constraint "constructor_return == top"
    // TODO: consider using preference constraints to express type rules that issue
    // warnings if violated.
    @SuppressWarnings("cast.unsafe.constructor.invocation")
    SimpleFileVisitor s = new SimpleFileVisitor<String>(){};

    OutterI.InnerI<Object> f = new OutterI.InnerI<Object>() {};

    A a = new @OsUntrusted A() {};
}

interface OutterI<T> {
    @OsTrusted
    public interface InnerI<T> {}
}

class A {}
