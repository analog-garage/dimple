function [FGraph] = deserializeFromXML(FileName)
    s = com.analog.lyric.dimple.model.FactorGraph.deserializeFromXML(FileName);
    FGraph = FactorGraph('nestedGraph',com.analog.lyric.dimple.matlabproxy.PFactorGraph(s));
end

