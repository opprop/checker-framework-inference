import nninf.qual.Nullable;

class FixableError1 {
    private String id = "1234";

    public void setId(@Nullable String id) {
        // :: fixable-error: (assignment.type.incompatible)
        this.id = id;

        // :: fixable-error: (argument.type.incompatible)
        recordIDs(id);
    }

    public void recordIDs(String id) {}
}
