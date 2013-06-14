package com.analog.lyric.dimple.parameters;

import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.options.IEnumOptionKey;
import com.analog.lyric.options.IOptionKey;

/**
 * Key identifying a parameter value in a {@link IParameterList}.
 * <p>
 * As with {@link IOptionKey}, concrete instances should either be enum instances
 * or static final field values with {@link #name} and {@link #getDeclaringClass()}
 * values matching the class and field.
 * <p>
 * See {@link ParameterKey} for example of keys defined using static field instances
 * of that class.
 * <p>
 * The enum technique requires more code because enums cannot extend a class containing
 * default implementations. However, most of the code can be copied and the keys do not
 * need to explicitly specify the ordinal, declaring class or name, and the enum class's
 * static {@code values()} method can be used to provide the list of keys required when
 * implementing {@link IParameterList#getKeys()}.
 * <pre>
 * public enum GuassianParameter implements IParameterKey
 * {
 *     mean(0, RealDomain.full()),
 *     precision(1, RealDomain.nonNegative());
 *
 *     // Everything below can be copied except for the constructor name
 *     private final double _defaultValue;
 *     private final RealDomain _domain;
 *
 *     private GuassianParameter(double defaultValue, RealDomain domain)
 *     {
 *          _defaultValue = defaultValue;
 *          _domain = domain;
 *     }
 *
 *     public Class<Double> type() { return Double.class; }
 *     public Double defaultValue() { return _defaultValue; }
 *     public Double lookup(IOptionHolder holder) { return holder.options().lookup(this); }
 *     public void set(IOptionHolder holder, Double value) { holder.options().set(this,  value); }
 *     public void unset(IOptionHolder holder) { holder.options().unset(this); }
 *     public RealDomain domain() { return _domain; }
 * }
 * </pre>
 */
public interface IParameterKey extends IEnumOptionKey<Double>
{
	/**
	 * The expected domain of values associated with this key. Used to validate new values.
	 */
	public abstract RealDomain domain();
}
