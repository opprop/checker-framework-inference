package ostrusted.qual;

import org.checkerframework.framework.qual.PolymorphicQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Polymorphic qualifier for {@code OsTrusted}.
 *
 * <p>See https://eisop.github.io/cf/manual/manual.html#method-qualifier-polymorphism for
 * information on the semantics of polymorphic qualifiers in the checker framework.
 *
 * <p>
 *
 * @see OsTrusted
 * @see OsUntrusted
 */
@Documented
@PolymorphicQualifier(OsUntrusted.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface PolyOsTrusted {}
