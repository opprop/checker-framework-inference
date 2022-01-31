package nninf.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.QualifierForLiterals;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.UpperBoundFor;
import org.checkerframework.framework.qual.TypeKind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see Nullable
 * @see org.checkerframework.checker.nullness.qual.NonNull
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Nullable.class)
@QualifierForLiterals(value = {LiteralKind.STRING, LiteralKind.PRIMITIVE})
@DefaultFor(TypeUseLocation.EXCEPTION_PARAMETER)
@DefaultQualifierInHierarchy
@UpperBoundFor(
        typeKinds = {
                TypeKind.PACKAGE,
                TypeKind.INT,
                TypeKind.BOOLEAN,
                TypeKind.CHAR,
                TypeKind.DOUBLE,
                TypeKind.FLOAT,
                TypeKind.LONG,
                TypeKind.SHORT,
                TypeKind.BYTE
        })
public @interface NonNull {}
