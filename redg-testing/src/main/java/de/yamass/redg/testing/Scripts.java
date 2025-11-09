package de.yamass.redg.testing;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({SharedDatabasesInvocationProvider.class})
public @interface Scripts {

	String[] value() default {};

}
