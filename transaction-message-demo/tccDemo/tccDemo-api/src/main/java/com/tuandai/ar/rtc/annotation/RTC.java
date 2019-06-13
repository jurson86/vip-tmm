package com.tuandai.ar.rtc.annotation;

import com.tuandai.ar.rtc.enums.PropagationEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Gus Jiang
 * @date 2019/3/11  15:53
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RTC {

    /**
     * Confirm method string.
     *
     * @return the string
     */
    String confirmMethod() default "";

    /**
     * Cancel method string.
     *
     * @return the string
     */
    String cancelMethod() default "";

    /**
     * propagation
     * @return PropagationEnum
     */
    PropagationEnum propagation() default PropagationEnum.PROPAGATION_REQUIRED;
}
