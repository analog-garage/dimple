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

classdef Complex < RealJoint
   methods
       function obj = Complex(varargin)
           obj@RealJoint(2,varargin{:});
       end
   end
   
   
   methods (Access=protected)
      
       % Turn the values into complex numbers
       function v = getValue(obj)
            values = getValue@RealJoint(obj);
            
            % Final dimension of values array have the real and imaginary parts
            arrayDims = ndims(values);
            if (arrayDims == 2 && min(size(values)) == 1)
                arrayDims = 1;  % ndims answers 2 even if it's really 1
            end
            rindex = repmat({':'},1,arrayDims);
            iindex = rindex;
            rindex{arrayDims} = 1;
            iindex{arrayDims} = 2;
            v = values(rindex{:}) + 1i*values(iindex{:});
       end
       
       % Treat the set values as complex numbers
       function setFixedValue(obj,value)
           arrayDims = ndims(value);
           if (arrayDims == 2 && min(size(value)) == 1)
               arrayDims = 1;  % ndims answers 2 even if it's really 1
           end
           valueArray = cat(arrayDims+1, real(value), imag(value));
           setFixedValue@RealJoint(obj,valueArray);
       end
        
       % Turn the return values into complex nubmers
       function v = getFixedValue(obj)
            values = getFixedValue@RealJoint(obj);
            
            % Final dimension of values array have the real and imaginary parts
            arrayDims = ndims(values);
            if (arrayDims == 2 && min(size(values)) == 1)
                arrayDims = 1;  % ndims answers 2 even if it's really 1
            end
            rindex = repmat({':'},1,arrayDims);
            iindex = rindex;
            rindex{arrayDims} = 1;
            iindex{arrayDims} = 2;
            v = values(rindex{:}) + 1i*values(iindex{:});
        end
        
        % Treat the set values as complex numbers
       function setGuess(obj,value)
           arrayDims = ndims(value);
           if (arrayDims == 2 && min(size(value)) == 1)
               arrayDims = 1;  % ndims answers 2 even if it's really 1
           end
           valueArray = cat(arrayDims+1, real(value), imag(value));
           setGuess@RealJoint(obj,valueArray);
       end
        
       % Turn the return values into complex nubmers
       function v = getGuess(obj)
            values = getGuess@RealJoint(obj);
            
            % Each cell has real and imaginary parts
            v = cellfun(@(x){x(1) + 1i*x(2)}, values);
        end



   end
   
end
