package trusted.qual;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A type annotation indicating that the contained value cannot be proven to be trustworthy.
 *
 * <p>Variables with no annotation are considered {@code Untrusted}.
 *
 * <p>It is up to the user to determine what, exactly, she wants {@code Untrusted} to represent.
 * Similar type systems with prescribed meanings are available in other packages.
 *
 * <p>
 *
 * @see Trusted
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface Untrusted {}
