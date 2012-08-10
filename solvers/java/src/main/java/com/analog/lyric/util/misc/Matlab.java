package com.analog.lyric.util.misc;

/**
 * This annotation is used to mark public Java members that are expected to be invoked
 * directly from MATLAB. Such members cannot be removed or refactored without taking its
 * potential MATLAB use into account.
 */
public @interface Matlab
{

}
