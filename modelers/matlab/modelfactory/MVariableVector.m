classdef MVariableVector < handle
    
    properties
        Solver;
        VarIds;
    end
    
    
    methods
        function obj = MVariableVector(varargin)
            if nargin == 4
                className = varargin{1};
                domain = varargin{2};
                numels = varargin{3};
                solver = varargin{4};
                
                obj.Solver = solver;
                if numels == 0
                    obj.VarIds = [];
                else
                    obj.VarIds = obj.Solver.createVariables(className,domain.Elements,numels);
                end
            elseif nargin == 2
                varIds = varargin{1};
                solver = varargin{2};
                obj.VarIds = varIds;
                obj.Solver = solver;
                obj.Solver.addMultipleVariableRef(obj.VarIds);
            else
                error('invalid constructor call');
            end
            

        end
        
        function setInput(obj,varids,inputs)
            realVarIds = obj.VarIds(varids+1);
            obj.Solver.setMultipleVariableInput(realVarIds,inputs);
        end
        
        function beliefs = getBeliefs(obj,varids)
            varIds = obj.VarIds(varids+1);
            beliefs = obj.Solver.getMultipleVariableBelief(varIds);            
        end
        
        function output = getSlice(obj,varIds)
            realVarIds = obj.VarIds(varIds+1);
            output = MVariableVector(realVarIds,obj.Solver);
        end
        
        function inputs = getInput(obj,varIds)
            error('not supported');
        end
        
        function output = concat(obj,varMats,varMatIndices,varIndices)            
            
            if nargin == 4
                ids = zeros(numel(varIndices),1);
                for i = 1:length(varIndices)
                    ids(i) = varMats{varMatIndices(i)+1}.VarIds(varIndices(i)+1);
                end
            else
                ids = [];
                for i = 1:length(varMats)
                   ids = [ids; reshape(varMats{i}.VarIds,numel(varMats{i}.VarIds),1)]; 
                end
            end
            output = MVariableVector(ids,obj.Solver);
        end
        
        function sz = size(obj)
            sz = numel(obj.VarIds);
        end
        
        function setPrimitivePolynomial(obj,poly)
           obj.Solver.setPrimitivePolynomial(obj.VarIds,poly); 
        end
        
        function delete(obj)
           obj.Solver.removeMultipleVariableRef(obj.VarIds); 
        end
        
        function variable = getVariable(obj,ind)
           variable = MVariable(obj.Solver,obj.VarIds(ind+1)); 
        end
    end
    
end

