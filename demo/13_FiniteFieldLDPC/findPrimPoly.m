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


function [ primPoly ] = findPrimPoly( degree )
% Output an (integer representation of a) primitive polynomial for 
% generating the finite field with 2^dim elements

listPrimPoly=zeros(24);
listPrimPoly(3)=2^3 + 2^1 + 2^0;
listPrimPoly(4)=2^4 + 2^1 + 2^0;
listPrimPoly(5)=2^5 + 2^2 + 2^0;
listPrimPoly(6)=2^6 + 2^1 + 2^0;
listPrimPoly(7)=2^7 + 2^1 + 2^0;
listPrimPoly(8)=2^8 + 2^4 + 2^3 + 2^2 + 2^0;
listPrimPoly(9)=2^9 + 2^4 + 2^0;
listPrimPoly(10)=2^10 + 2^3 + 2^0;
listPrimPoly(11)=2^11 + 2^2 + 2^0;
listPrimPoly(12)=2^12 + 2^6 + 2^4 + 2^1 + 2^0;
listPrimPoly(13)=2^13 + 2^4 + 2^3 + 2^1 + 2^0;
listPrimPoly(14)=2^14 + 2^5 + 2^3 + 2^1 + 2^0;
listPrimPoly(15)=2^15 + 2^1 + 2^0;
listPrimPoly(16)=2^16 + 2^5 + 2^3 + 2^2 + 2^0;
listPrimPoly(17)=2^17 + 2^3 + 2^0;
listPrimPoly(18)=2^18 + 2^5 + 2^2 + 2^1 + 2^0;
listPrimPoly(19)=2^19 + 2^5 + 2^2 + 2^1 + 2^0;
listPrimPoly(20)=2^20 + 2^3 + 2^0;
listPrimPoly(21)=2^21 + 2^2 + 2^0;
listPrimPoly(22)=2^22 + 2^1 + 2^0;
listPrimPoly(23)=2^23 + 2^5 + 2^0;
listPrimPoly(24)=2^24 + 2^4 + 2^3 + 2^1 + 2^0;


if and(degree>=3,  degree<=24)
    primPoly=listPrimPoly(degree);
else
    error('Unsupported field size\n');
end

end

