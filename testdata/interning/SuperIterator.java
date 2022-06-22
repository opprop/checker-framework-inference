import java.util.Iterator;
import java.util.List;
class SuperIterator<T extends Iterable<E>, E> implements
        Iterator<SuperIterator.SuperIteratorEntry> {
    private List<T> iterable;
    private Iterator<E> current, next;
    private int currentIndex = 0, nextIndex = 0;
    private SuperIteratorEntry<E> entry;
    public SuperIterator(List<T> iterable) {
        this.iterable = iterable;
        entry = new SuperIteratorEntry<E>();
                if (iterable.size() == 0) {
            current = new DummyIterator();
            next = new DummyIterator();
        } else {
                        next = iterable.get(nextIndex).iterator();
            moveNext();
                        current = iterable.get(currentIndex).iterator();
            moveCurrent();
                        if (next.hasNext())
                next.next();
        }
    }
    private void moveNext() {
        while (nextIndex < iterable.size() - 1 && !next.hasNext())
            next = iterable.get(++nextIndex).iterator();
    }
    private void moveCurrent() {
        while (currentIndex < iterable.size() - 1 && !current.hasNext())
            current = iterable.get(++currentIndex).iterator();
    }
    public boolean hasNext() {
        return current.hasNext() || next.hasNext();
    }
    public SuperIteratorEntry<E> next() {
                entry.update(currentIndex, current.next());
                moveCurrent();
                moveNext();
        if (next.hasNext())
            next.next();
        return entry;
    }
    public void remove() {
        current.remove();
    }
    private class DummyIterator implements Iterator<E> {
        public boolean hasNext() {
            return false;
        }
        public E next() {
            return null;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    public static class SuperIteratorEntry<F> {
        private int i;
        private F o;
        void update(int i, F o) {
            this.i = i;
            this.o = o;
        }
        public int index() {
            return i;
        }
        public F get() {
            return o;
        }
    }
}
