%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices, Inc.
%
%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

classdef MultiplexerCPD < FactorGraph

    properties(Access=public)

       Y;
       Zs;
       A;
    end
    
    methods
        
        function obj = MultiplexerCPD(varargin)

            modeler = getModeler();
            tmp = modeler.getMultiplexerCPD(varargin);
            obj@FactorGraph('VectorObject',tmp);

        end
    
        
        function a = get.A(obj)
            a = wrapProxyObject(obj.VectorObject.getA());
        end
        
        function y = get.Y(obj)
            y = wrapProxyObject(obj.VectorObject.getY());
        end
        
        function zs = get.Zs(obj)
           pzs = cell(obj.VectorObject.getZs());
           zs = cell(size(pzs));
           
           for i = 1:length(zs)
              zs{i} = wrapProxyObject(pzs{i}); 
           end
        end
    end
    
          
end

