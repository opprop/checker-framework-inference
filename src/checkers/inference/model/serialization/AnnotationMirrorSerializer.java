package checkers.inference.model.serialization;

import javax.lang.model.element.AnnotationMirror;

/** Interface to handle serialization and deserialization of AnnotationMirrors to Strings. */
public interface AnnotationMirrorSerializer {

    AnnotationMirror deserialize(String atm);

    String serialize(AnnotationMirror atm);
}
