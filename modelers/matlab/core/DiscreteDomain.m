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

classdef DiscreteDomain < Domain
    properties (SetAccess=immutable)
        Elements;
    end
    
    % TODO - instead of copying all elements from underlying Java object,
    % we should defer it to avoid extra work for large range domains.
    
    methods
        function obj = DiscreteDomain(elements)
            if ~iscell(elements)
                if isa(elements, 'com.analog.lyric.dimple.matlabproxy.PDiscreteDomain')
                    obj.IDomain = elements;
                    obj.Elements = cell(obj.IDomain.getElements())';
                    return;
                end
                newdomain = cell(1,numel(elements));
                for i = 1:numel(elements);
                    newdomain{i} = elements(i);
                end
                elements = newdomain;
            end
            obj.Elements = elements;
            
            modeler = getModeler();
            try
                obj.IDomain = modeler.createDiscreteDomain(elements);
            catch exception
                % FIXME - this is a bit of a hack! See bug 415
                newdomain = cell(size(elements));
                
                for i = 1:length(newdomain)
                    newdomain{i} = sprintf('domainitem%d',i);
                end
                
                obj.IDomain = modeler.createDiscreteDomain(newdomain);
            end
            
        end
        function val = isequal(obj,other)
            if ~isa(other,'DiscreteDomain')
                val = false;
            else
                val = isequal(obj.Elements,other.Elements);
            end
        end
        
        function result = isDiscrete(obj)
            result = true;
        end
    end
    
end
