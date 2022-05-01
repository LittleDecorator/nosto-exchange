package com.nosto.exchange.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.nosto.exchange.validator.CurrencyValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({FIELD, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {CurrencyValidator.class})
public @interface Currency {

  String message() default "Unsupported currency type is specified";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
