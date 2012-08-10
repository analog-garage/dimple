classdef DiscreteStream < VariableStreamBase 
    methods
        function obj = DiscreteStream(domain)
            if ~isa(domain,'Domain')
                domain = DiscreteDomain(domain);
            end
            
            model = getModeler();            
            IDiscreteStream = model.createDiscreteStream(domain.IDomain);
            
            obj@VariableStreamBase(IDiscreteStream);

        end
    end
end
