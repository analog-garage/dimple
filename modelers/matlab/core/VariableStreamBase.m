classdef VariableStreamBase < IVariableStreamSlice
    properties
        IVariableStream;
        DataSource;
        FirstVarIndex;
        LastVarIndex;
        FirstVar;
        LastVar;
        IVariableStreamSlice;
    end
    
    
    
    methods
        
        function obj = VariableStreamBase(IVariableStream)
            obj.IVariableStream = IVariableStream;
        end

        function ret = get.IVariableStreamSlice(obj)
            ret = obj.IVariableStream;
        end
        
        function set.DataSource(obj,dataSource)
           obj.IVariableStream.setDataSource(dataSource); 
        end
        
        function slice = getSlice(obj,startIndex,increment,endIndex)
            if nargin < 3
                increment = 1;
                endIndex = Inf;
            elseif nargin < 4
                endIndex = increment;
                increment = 1;
            end
            
            ISlice = obj.IVariableStream.getSlice(startIndex-1,increment,endIndex-1);
            
            slice = VariableStreamSlice(ISlice);
        end
        
        function var = get(obj,ind)
            ivar = obj.IVariableStream.get(ind-1);
            var = wrapProxyObject(ivar);
        end

        function ret = get.FirstVarIndex(obj)
            ret = obj.IVariableStream.getFirstVarIndex()+1;
        end
        function ret = get.LastVarIndex(obj)
            ret = obj.IVariableStream.getLastVarIndex()+1;
        end
        function ret = get.FirstVar(obj)
            ret = obj.IVariableStream.getFirstVar();
            ret = wrapProxyObject(ret);
        end
        
        function ret = get.LastVar(obj)
            ret = obj.IVariableStream.getLastVar();
            ret = wrapProxyObject(ret);
        end
        
        %{
        function b = subsref(obj,s)
            obj
            s
        end
        %}
    end
end
