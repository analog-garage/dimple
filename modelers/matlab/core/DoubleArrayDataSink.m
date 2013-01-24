classdef DoubleArrayDataSink < DataSink
    properties
        Array;
    end
    methods
        function obj = DoubleArrayDataSink(dataSink)
            if nargin == 0;
                dataSink = com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink();
            end
            obj@DataSink(dataSink);
        end
        
        function retval = hasNext(obj)
            retval = obj.IDataSink.hasNext();
        end
        function retval = getNext(obj)
            retval = obj.IDataSink.getNext();
        end
        
        function retval = get.Array(obj)
            retval = obj.IDataSink.getArray();
        end
    end
    
end
