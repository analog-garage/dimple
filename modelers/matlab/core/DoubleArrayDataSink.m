classdef DoubleArrayDataSink < DataSink
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
        
    end
    
end
