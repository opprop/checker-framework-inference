import java.util.List;

class Wildcards4 {
    private List<? extends List<? extends CharSequence>> other;
    private List<? extends List<? extends String>> test;

    public void context() {
        other = test;

        other.toString();
    }
}
