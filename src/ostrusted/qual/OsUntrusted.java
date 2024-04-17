package ostrusted.qual;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents data that may not be suitable to pass to OS commands such as exec.
 *
 * <p>Types are implicitly {@code OsUntrusted}.
 *
 * @see OsTrusted
 * @see PolyOsTrusted
 * @see trusted.qual.UnTrusted
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface OsUntrusted {}
