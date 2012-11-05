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

function testMatrixObject
    N = 3;
    values = cell(3^2,1);
    for i = 1:N^2
        values{i} = MyObject(i,[i i]);
    end

    mvo = MyVectorObject(values);
    mo = MyMatrix(mvo,reshape(0:(N^2-1),N,N));

    %Test retrieving indices
    mo2 = mo(1:2,1:2);
    for i = 1:2
        for j = 1:2
            assertEqual(mo2(i,j).VectorObject.ValueObjects{1},mo(i,j).VectorObject.ValueObjects{1});
        end
    end
    assertEqual(mo2(1,1).VectorObject.ValueObjects{1}.Value,1);
    assertEqual(mo2(2,1).VectorObject.ValueObjects{1}.Value,2);
    assertEqual(mo2(1,2).VectorObject.ValueObjects{1}.Value,4);
    assertEqual(mo2(2,2).VectorObject.ValueObjects{1}.Value,5);

    %Test chaining these together
    newValues = [7 8; 9 10];
    mo2.Value = newValues;
    assertEqual(mo(1:2,1:2).Value,newValues);

    %Test no brackets
    message = '';
    try
        mo2{1};
    catch e
        message = e.message;
    end
    assertEqual('brackets are not supported',message);

    %Test subsasgn in subsref
    newval = [20 21; 22 23];
    mo(1:2,1:2).Value = newval;
    assertEqual(newval,mo2.Value);

    %Test error
    message = '';
    try
        mo2{1} = 1;
    catch e
        message = e.message;
    end
    assertEqual('{} not supported',message);

    %test assigning subsets
    values = cell(4,1);
    for i = 1:4
        values{i} = MyObject(i+100,[i+100 i+100]);
    end
    mo3 = MyMatrix(MyVectorObject(values),reshape(0:3,2,2));

    mo([1 3],[1 3]) = mo3;
    assertEqual(mo([1 3],[1 3]).Value,[101 103; 102 104]);

    %test invalid assignment
    message = '';
    try
        mo(1:2,1:2) = [1 2; 3 4];
    catch e
        message = e.message;
    end
    assertEqual('must assign matrix objects',message);

    %test anotehr invalid assignment
    message = '';
    try
        mo4 = MyMatrix(MyVectorObject({MyObject(1e4,[1 1])}),0);
        mo(1,1) = mo4;
    catch e
        message = e.message;
    end
    assertEqual(message,'for the sake of the test dont allow this');

    %test Length
    assertEqual(length(mo),3);

    %test Size
    assertEqual(size(mo),[3 3]);

    %test end where n == 1
    mo(:).Value = 1:9;
    assertEqual(mo(1:end).Value,1:9);

    %test end where n ~=1
    assertEqual(mo(1:end,1:2).Value,reshape(1:6,3,2));

    %test repmat
    mo5 = mo(:);
    mo5.Value = 1:9;
    mo6 = repmat(mo5,1,3);
    mo6(1,1).Value = 10;
    assertEqual(mo6(:,1).Value,[10 2:9]');
    assertEqual(mo6(:,2).Value,[10 2:9]');
    assertEqual(mo6(:,3).Value,[10 2:9]');

    %test equality
    mo2 = mo(1:2,1:2);
    assertTrue(mo2 == mo(1:2,1:2));
    assertEqual(mo2,mo(1:2,1:2));

    %test transpose
    value = mo2.Value;
    mo3 = mo2.';
    assertEqual(mo3.Value,value');

    %test ctranspose
    value = mo2.Value;
    mo3 = mo2';
    assertEqual(mo3.Value,value');

    %test reshape
    mo = reshape(mo,9,1);
    assertEqual(size(mo),[9 1]);
    mo.Value = 1:9;
    mo = reshape(mo,3,3);
    assertEqual(mo.Value,reshape([1:9]',3,3));

    %test flipud
    value = mo.Value;
    mo = flipud(mo);
    assertEqual(mo.Value,flipud(value));

    %test fliplr
    value = mo.Value;
    mo = fliplr(mo);
    assertEqual(mo.Value,fliplr(value));

    %test horzcat 
    a = mo2(:,1);
    b = mo2(:,2);
    assertEqual([a b],mo2);

    %test vertcat
    a = mo2(1,:);
    b = mo2(2,:);
    assertEqual([a; b],mo2);
end

