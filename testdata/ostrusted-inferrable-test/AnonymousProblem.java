import ostrusted.qual.OsTrusted;
import ostrusted.qual.OsUntrusted;
import java.nio.file.SimpleFileVisitor;

@SuppressWarnings("cast.unsafe.constructor.invocation")
public class AnonymousProblem {

    SimpleFileVisitor s = new SimpleFileVisitor<String>(){};

    OutterI.InnerI<Object> f = new OutterI.InnerI<Object>() {};

    A a = new @OsUntrusted A() {};
}

interface OutterI<T> {
    @OsTrusted
    public interface InnerI<T> {}
}

class A {}
