function fg =  wrapJavaGraph(javaGraph)
    fg = FactorGraph('nestedGraph',com.analog.lyric.dimple.matlabproxy.PFactorGraph(javaGraph));
end
