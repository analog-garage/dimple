% unwrapProxyObject Unwraps Dimple object into its underlying Java proxy
%
%    unwrapProxyObject(objects)
%
% objects will only be processed if it is a single MATLAB object or a cell array of
% MATLAB objects, otherwise its value will simply be passed through.
%
% Each object is unwrapped by invoking its 'getProxyObject'. If it has
% no such method, it will simply be passed through.
%
% This is for use internally in the Dimple MATLAB implementation for
% communicating with the Java layer.
%
% See also wrapProxyObject

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2015 Analog Devices, Inc.
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

function result = unwrapProxyObject(obj)
    
    result = obj;
    
    if isempty(obj)
        return;
    end
       
    if iscell(obj)
        result = cellfun(@unwrapProxyObject, obj, 'UniformOutput', false);
        return;
    end

    if isobject(obj) && ismethod(obj,'getProxyObject')
        result = obj.getProxyObject();
    end

end