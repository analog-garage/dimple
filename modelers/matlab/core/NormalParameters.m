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

classdef NormalParameters < ParameterizedMessage
    properties
        Mean;
        Precision;
        Variance;
        StandardDeviation;
    end
    methods
        function obj = NormalParameters(mean, precision)
            if ~isnumeric(mean)
                obj.IParameters = mean;
            else
                modeler = getModeler();
                obj.IParameters = modeler.createNormalParameters(mean,precision);
            end
        end
        
        function mean = get.Mean(obj)
           mean = obj.IParameters.getMean(); 
        end
        
        function precision = get.Precision(obj)
           precision = obj.IParameters.getPrecision(); 
        end
        
        function variance = get.Variance(obj)
           variance = obj.IParameters.getVariance(); 
        end
        
        function stdev = get.StandardDeviation(obj)
           stdev = obj.IParameters.getStandardDeviation(); 
        end
        
        
        %*********************************************************
        % The following methods are for backward compatibility with
        % the earlier array representation of Normal parameters
        % that list the mean and standard-deviation.  The following
        % methods allow accessing elements of this class as if it were
        % an array of these two items, and also allows making comparisons
        % using ==, ~=, and assertElementsAlmostEqual.
        % Doing this is kind of a hack, and perhaps should be removed
        % in some future version when the original format need
        % no longer be supported.
        %*********************************************************

        % For backward compatibility
        function varargout = subsref(obj,S)
            switch S(1).type
                case '()'
                    array = [obj.Mean; obj.StandardDeviation];
                    varargout = {array(S.subs{1})};
                otherwise
                    [varargout{1:nargout}] = builtin('subsref',obj,S);
            end
        end
        
        % For backward compatibility
        function result = eq(a, b)
            if (isa(a, 'NormalParameters'))
                result = (a.toArray(a) == a.toArray(b));
            elseif (isa(b, 'NormalParameters'))
                result = (b.toArray(a) == b.toArray(b));
            else
                result = (a == b);
            end
        end
        
        % For backward compatibility
        function result = ne(a, b)
            if (isa(a, 'NormalParameters'))
                result = (a.toArray(a) ~= a.toArray(b));
            elseif (isa(b, 'NormalParameters'))
                result = (b.toArray(a) ~= b.toArray(b));
            else
                result = (a ~= b);
            end
        end
        
        % For backward compatibility
        function diff = minus(a, b)
            if (isa(a, 'NormalParameters'))
                diff = a.toArray(a) - a.toArray(b);
            elseif (isa(b, 'NormalParameters'))
                diff = b.toArray(a) - b.toArray(b);
            else
                diff = a - b;
            end
        end
        
        % For backward compatibility
        function is = isfloat(obj)
            is = true;
        end

        % For backward compatibility
        function c = class(obj)
            c = 'double';
        end
        
        % For backward compatibility
        function s = size(obj)
            s = [2 1];
        end

    end
    
    methods (Access=private)
        
        function array = toArray(obj, in)
            if (isa(in, 'NormalParameters'))
                S.type = '()';
                S.subs = {':'};
                array = in.subsref(S);
            else
                array = in;
            end
        end
        
    end
end
