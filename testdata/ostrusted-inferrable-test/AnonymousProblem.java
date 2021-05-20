import ostrusted.qual.OsTrusted;
import java.nio.file.SimpleFileVisitor;

@SuppressWarnings("cast.unsafe.constructor.invocation")
public class AnonymousProblem {

    SimpleFileVisitor s = new SimpleFileVisitor<String>(){};

    // :: fixable-error: (assignment.type.incompatible)
    OutterI.InnerI<Object> f = new OutterI.InnerI<Object>() {};
}

interface OutterI<T> {
    @OsTrusted
    public interface InnerI<T> {}
}
