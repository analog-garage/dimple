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

classdef MComboTable < handle
    properties
        %Id;
        Name;
        Table;
        Values;
        Id;
    end
   methods
       function obj = MComboTable(name,table,values,domainobjs)
           %function tableFactor = createTableFactorFunction(obj,name,table,values,domainobjs)

           
           obj.Id = 0;
           %gMComboTableNextId___ = gMComboTableNextId___+1;
           
           obj.Name = name;
           obj.Table = table;
           obj.Values = values;
       end
       
   end
    
end
