/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.util.misc;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes default null annotation for contained elements.
 * <p>
 * Indicates that elements within the package, type, method or constructor that was marked
 * with this annotation should be considered to implicitly be marked as {@link NonNull} if
 * not explicitly marked otherwise.
 * <p>
 * This is for static analysis by JDT in Eclipse.
 * <p>
 * @since 0.06
 * @author Christopher Barber
 */
@Documented
@Retention(value=CLASS)
@Target(value={PACKAGE, TYPE, METHOD, CONSTRUCTOR})
public @interface NonNullByDefault
{
	boolean value() default true;
}
