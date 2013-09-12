classdef PLLearner < handle
    properties
        IPLLearner;
    end
    methods
        function obj = PLLearner(fg,tables,vars)
            
            ifg = fg.VectorObject.getModelerNode(0);
            fts = javaArray('com.analog.lyric.dimple.factorfunctions.core.IFactorTable',length(tables));
            for i = 1:length(tables)
                fts(i) = tables{i}.ITable.getModelerObject();
            end
            
            for i = 1:length(vars)
                vars{i} = vars{i}.VectorObject;
            end
            
            vars = com.analog.lyric.dimple.matlabproxy.PHelpers.convertToVariableArray(vars);

            obj.IPLLearner = com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood.PseudoLikelihood(ifg,fts,vars);

        end
        
        function learn(obj,data,args)
            if nargin > 2
                if isfield(args,'scaleFactor');
                    scaleFactor = args.scaleFactor;
                else
                    scaleFactor = 1;
                end
                
                if isfield(args,'numSteps')
                    numSteps = args.numSteps;
                else
                    numSteps = 1;
                end
                
            else
                scaleFactor = 1;
                numSteps = 1;
            end
            
            %TODO: assumes indices. Eventually handle domain values.
            obj.IPLLearner.learn(int32(data),numSteps,scaleFactor);
        end
        
        function setData(obj,data)
            obj.IPLLearner.setData(int32(data));
        end
        
        function result = calculateGradient(obj)
            result = obj.IPLLearner.calculateGradient();
        end
        
        function result = calculateNumericalGradient(obj, table, weightIndex, delta)
            result = obj.IPLLearner.calculateNumericalGradient(table.ITable.getModelerObject(),weightIndex-1,delta);
        end
        
        function result = calculatePseudoLikelihood(obj)
            result = obj.IPLLearner.calculatePseudoLikelihood();
        end

    end
    
end