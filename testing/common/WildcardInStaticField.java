import java.util.ArrayList;
import java.util.List;

class StaticMethod {
    public static List<?> something;

    public static void method(String param) {
        String other = param;
        something = new ArrayList<String>();
    }

    public void instMethod() {
        method("YUM");
    }
}
