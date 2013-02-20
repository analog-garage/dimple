classdef DoubleArrayDataSink < DataSink
    properties
        Array;
    end
    methods
        function obj = DoubleArrayDataSink(dimensions,dataSink)
            
            if nargin < 1
                dimensions = size(1);
            end
            
            if nargin < 2                
                modeler = getModeler();
                dataSink = modeler.getDoubleArrayDataSink(prod(dimensions));
            end
            obj@DataSink(dimensions,dataSink);
        end
       
        function array = get.Array(obj)
           array = obj.IDataSink.getArray(); 
           array = MatrixObject.unpack(array,obj.Indices,1);
        end
    end
    
end
