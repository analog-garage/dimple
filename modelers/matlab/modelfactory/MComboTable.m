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
