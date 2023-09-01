import checkers.tainting.quals.*;

class GenericClass<@Untainted T, @Tainted E> {

    void test() {
        new GenericClass<T, T>();
    }
}
