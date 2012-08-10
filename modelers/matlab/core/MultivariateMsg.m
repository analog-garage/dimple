classdef MultivariateMsg < Msg
    properties
        Means;
        Covariance;
    end
    methods
        function obj = MultivariateMsg(means,covariance)
            modeler = getModeler();
            obj.IMsg = modeler.createMultivariateMsg(means,covariance);
        end
        
        function means = get.Means(obj)
           means = obj.IMsg.getMeans(); 
        end
        
        function covar = get.Covariance(obj)
           covar = obj.IMsg.getCovariance(); 
        end
        
    end
end
