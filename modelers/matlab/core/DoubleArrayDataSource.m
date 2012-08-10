function out = DoubleArrayDataSource(data)
    if nargin < 1
        out = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource();
    else
        out = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource(data);
    end       

end
