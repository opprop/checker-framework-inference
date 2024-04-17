package nninf.qual;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Target;

/**
 * A reference for which we don't know whether it's a key for a map or not.
 *
 * <p>Programmers cannot write this in source code.
 */
@InvisibleQualifier
@SubtypeOf({})
@Target({}) // empty target prevents programmers from writing this in a program
@DefaultQualifierInHierarchy
public @interface UnknownKeyFor {}
