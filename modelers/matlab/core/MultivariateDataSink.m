%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2013 Analog Devices, Inc.
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

classdef MultivariateDataSink < DataSink
    properties
        Array;
    end
    methods
        function obj = MultivariateDataSink(dataSink)
            if nargin == 0;
                dataSink = com.analog.lyric.dimple.model.repeated.MultivariateDataSink();
            end
            obj@DataSink(dataSink);
        end
        
        function retval = hasNext(obj)
            retval = obj.IDataSink.hasNext();
        end
        function retval = getNext(obj)
            retval = MultivariateMsg(obj.IDataSink.getNext());
        end
        
        function retval = get.Array(obj)
            tmp = obj.IDataSink.getArray();
            retval = cell(size(tmp));
            for i= 1:length(retval)
                retval{i} = MultivariateMsg(tmp(i));
            end
        end
    end
end