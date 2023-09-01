import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class IteratorAssignment {

    public void containerMethod() {
        final List<Integer> intList = new ArrayList<Integer>();
        final Iterator<Integer> intIterator = intList.iterator();

        while (intIterator.hasNext()) {
            final Integer quality = intIterator.next();
        }
    }
}
