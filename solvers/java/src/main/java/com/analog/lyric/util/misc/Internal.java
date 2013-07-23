package com.analog.lyric.util.misc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark public Java members that are intended to only be
 * used internally within the project/jar in which it is defined.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal
{
}
