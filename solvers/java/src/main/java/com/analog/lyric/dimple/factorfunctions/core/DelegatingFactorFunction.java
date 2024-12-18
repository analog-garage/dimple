package com.analog.lyric.dimple.factorfunctions.core;

import com.analog.lyric.dimple.model.values.Value;

/**
 * Base class for factor functions that delegate to an {@link IFactorFunction}.
 *
 * @since 0.8.1
 */
public class DelegatingFactorFunction extends FactorFunction {

    private final IFactorFunction _delegate;

    public DelegatingFactorFunction(IFactorFunction delegate) {
        super(delegate.getName());
        _delegate = delegate;
    }

    @Override
    public boolean isDeterministicDirected() {
        return _delegate.isDeterministicDirected();
    }

    @Override
    public boolean isDirected() {
        return _delegate.isDirected();
    }

    @Override
    public boolean isParametric() {
        return _delegate.isParametric();
    }

    @Override
    public void evalDeterministic(Value[] values) {
        _delegate.evalDeterministic(values);
    }

    @Override
    public double evalEnergy(Value[] values) {
        return _delegate.evalEnergy(values);
    }
}
