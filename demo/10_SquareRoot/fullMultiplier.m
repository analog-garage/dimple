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

function isvalid = fullMultiplier(x,y,m_in,c_in,m_out,c_out)

    % This factor is the multiplicative analog of a full adder; it
    % enforces:
    %      2*c_out + m_out  = x*y + m_in + c_in
    % where all the variables are bits.
    
    isvalid = (((x & y & m_in) | (x & y & c_in) | (m_in & c_in))  == c_out) & ...
               (bitxor(x & y,bitxor(m_in,c_in)) == m_out);
    
end
