classdef BitStream < DiscreteStream
    
   methods
       function obj = BitStream()
           obj@DiscreteStream({0,1});
       end
   end
end
