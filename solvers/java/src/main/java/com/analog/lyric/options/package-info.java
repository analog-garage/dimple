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

/**
 * Statically typed hierarchical option support.
 * <p>
 * This package provides a type-safe framework for getting/setting simple option settings
 * in a hierarchical fashion.
 * <p>
 * Options are indexed by instances of the {@linkplain com.analog.lyric.options.IOptionKey IOptionKey}
 * interface that are declared as public static final fields of some publicly accessible class.
 * Typically such classes will contain nothing other than option declarations and will make use
 * of the concrete subclasses of {@linkplain com.analog.lyric.options.OptionKey OptionKey} provided
 * by this package. Note that the option key interface allows you to specify default values for
 * options for use when the option has not been explicitly set.
 * It is also useful to organize these classes in a hierarchical fashion inheriting
 * from {@linkplain com.analog.lyric.options.OptionKeyDeclarer OptionKeyDeclarer}. This allows Java IDE
 * users to easily visualize available options in the class hierarchy viewer. Here is a simple example:
 * <p>
 * <blockquote>
 * <pre>
 * public class GuiOptions extends OptionKeyDeclarer
 * {
 *     /**
 *      * <em>Document your option here</em>
 *      *&#047;
 *     public final StringOptionKey color =
 *         new StringOptionKey(MyOptions.class, "color", "white");
 * }
 * </pre>
 * </blockquote>
 * <p>
 * Sometimes when an option only applies to a single class, it may be easier to define
 * the option key in that class directly rather than in a separately defined option declaration
 * class.
 * <p>
 * Classes that support getting and setting options must implement the
 * {@linkplain com.analog.lyric.options.IOptionHolder IOptionHolder} interface and will
 * typically extend {@linkplain com.analog.lyric.options.LocalOptionHolder LocalOptionHolder},
 * which provides the ability to store options locally in the object.
 * <p>
 * Options can be get or set using the corresponding key with an option holder and can
 * be done either through methods on the key or the holder. Here are two equivalent ways
 * to set a color option on a widget object:
 * <p>
 * <blockquote>
 * <pre>
 *     if (GuiOptions.color.get(widget).equals("blue"))
 *     {
 *         GuiOptions.color.set(widget, "red");
 *     }
 * 
 *     if (widget.getOption(GuiOptions.color).equals("blue"))
 *     {
 *         widget.setOption(GuiOptions.color, "red");
 *     }
 * </pre>
 * </blockquote>
 * <p>
 * Some option key classes may support additional variants of the
 * {@linkplain com.analog.lyric.options.IOptionKey#set set}
 * method to automatically combine or convert arguments, which may make is desirable to set the option through
 * the key's method rather than {@linkplain com.analog.lyric.options.IOptionHolder#setOption setOption}. For instance,
 * the {@linkplain com.analog.lyric.options.StringListOptionKey StringListOptionKey} class overloads the {@code set}
 * method to directly take strings as arguments:
 * <blockquote>
 * <pre>
 *     // Shorter version using key-specific set method:
 *     MyOptions.names.set(widget, "foo", "bar", "baz");
 * 
 *     // Longer version using generic setOption method:
 *     widget.setOption(MyOptions.names, new OptionStringList("foo", "bar", baz"));
 * </pre>
 * </blockquote>
 * <p>
 * If you want an object to inherit option values from an other object in a hierarchical
 * fashion, you can implement the {@linkplain com.analog.lyric.options.IOptionHolder#getOptionParent()
 * getOptionParent()} method to indicate the default provider for option values. For instance, in a
 * GUI framework you might want components to inherit attributes such as background color or font size
 * from elements they are contained in. You can also implement more sophisticated inheritance schemes
 * by overriding {@linkplain com.analog.lyric.options.IOptionHolder#getOptionDelegates() getOptionDelegates()}.
 * <p>
 * @author Christopher Barber
 * @since 0.07
 */
@NonNullByDefault
package com.analog.lyric.options;
import org.eclipse.jdt.annotation.NonNullByDefault;


