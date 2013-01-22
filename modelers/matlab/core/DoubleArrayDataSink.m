function out = DoubleArrayDataSink()
    if nargin < 1
        out = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink();
    else
        out = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink(data);
    end            
end
