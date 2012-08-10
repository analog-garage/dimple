classdef RealStream < VariableStreamBase 
    methods
        function obj = RealStream()
            model = getModeler();            
            
            domain = RealDomain(-Inf,Inf);
                        
            IDiscreteStream = model.createRealStream(domain.IDomain);
            
            obj@VariableStreamBase(IDiscreteStream);

        end
    end
end
