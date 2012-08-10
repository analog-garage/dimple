classdef RealJoint < VariableBase
    
    properties
        Input;
        Belief;
    end
    
    methods
        function obj = RealJoint(varargin)
            %first arg can be a number
            %or it can be a domain
            %for now, only take one argument
            

            if length(varargin) == 4 && strcmp('existing',varargin{2})
                obj.Domain = varargin{1};
                obj.VarMat = varargin{3};
                obj.Indices = varargin{4};
            else

                if numel(varargin) ~= 1
                    raise('only support one arg for now');
                end
                domain = RealJointDomain(varargin{1});
                obj.Domain = domain;
                numEls = 1;
                modeler = getModeler();
                varMat = modeler.createRealJointVariableVector(class(obj),domain.IDomain,numEls);
                obj.VarMat = varMat;
                obj.Indices = 0;
            end
            
        end
        
        
        function x = get.Input(obj)
            x = obj.getInput();
        end
        function x = get.Belief(obj)
            x = obj.getBelief();
        end
        
        function var = createVariable(obj,domain,varMat,indices)
            var = RealJoint(domain,'existing',varMat,indices);
        end
    end
    
    methods (Access=protected)
        
        
        function b = getBelief(obj)
            sz = size(obj);
            
            b = cell(sz);
            
            a = cell(obj.VarMat.getBeliefs(obj.Indices));
            
            if prod(sz) == 1
                m = MultivariateMsg(0,0);
                m.IMsg = a{1};
                b = m;
            else
                for i = 1:numel(b)
                    m = MultivariateMsg(0,0);
                    m.IMsg = a{i};
                    b{i} = m;
                end
            end
        end
        
        function setInput(obj,input)
            v = obj.Indices;
            
            if isa(input,'Msg')
                input = input.IMsg;
            end
            
            varids = reshape(v,numel(v),1);
            obj.VarMat.setInput(varids,input);
        end
        
        function retval = getInput(obj)
            raise('not implemented');
        end
        
        
        function values = getValue(obj)
            raise('not implemented');
        end
        
    end
    
end
