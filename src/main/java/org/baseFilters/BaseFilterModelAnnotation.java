package org.baseFilters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseFilterModelAnnotation {
    String CSVDelimiter() default ",";

    String CSVEscapeChar() default "\"";

    String IsNeededSample() default "false"; // If Any Filter Need Sample , Always <First Filter> On <Initial Filter> list execute under sample resource //

    String IsIgnoreFirstLine() default "true"; // Ignore First Line Data On Files //
}
