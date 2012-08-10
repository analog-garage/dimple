classdef RealJointStream < VariableStreamBase 
    methods
        function obj = RealJointStream(numVars)
            model = getModeler();            
            
            domain = RealJointDomain(numVars);
                        
            stream = model.createRealJointStream(domain.IDomain);
            
            obj@VariableStreamBase(stream);

        end
    end
end
