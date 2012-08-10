classdef ComplexVar < RealJoint
   methods
       function obj = ComplexVar(varargin)
           obj@RealJoint(2,varargin{:});
       end
   end    
end
