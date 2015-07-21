% wrapProxyObject Wraps Dimple Java proxy object with appropriate MATLAB class.
%
%    wrapProxyObject(objects)
%
% This is for use internally in the Dimple MATLAB implementation for
% communicating with the Java layer.
%
% See also unwrapProxyObject

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012-2015 Analog Devices, Inc.
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

function result = wrapProxyObject(proxyObject)

    if isempty(proxyObject)
        result = [];
        
    elseif iscell(proxyObject)
        result = cellfun(@wrapProxyObject, proxyObject, 'UniformOutput', false);
    
    elseif isjava(proxyObject)
        % Look for @Matlab(wrapper="") annotation for object's class and
        % use that if found.
        wrapperName = char(com.analog.lyric.util.misc.MatlabUtil.wrapper(proxyObject));
        if (isempty(wrapperName))
            error('not supported');
        else
            result = feval(wrapperName, proxyObject);
        end
    
    else
        % Pass regular MATLAB objects through
        result = proxyObject;
    end
end
