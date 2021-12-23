// The test for https://github.com/opprop/checker-framework-inference/pull/366

public interface ConstantArray {
    String[] ACCESS_NAMES = {
        "public", "private", "protected", "static", "final", "synchronized",
        "volatile", "transient", "native", "interface", "abstract", "strictfp",
        "synthetic", "annotation", "enum"
    };
}

