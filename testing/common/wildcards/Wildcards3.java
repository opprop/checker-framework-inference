import java.util.List;

class Wildcards3 {
    private List<? extends List<? extends String>> other;
    private List<? extends List<String>> test;

    public void context() {
        other = test;

        other.toString();
    }
}
