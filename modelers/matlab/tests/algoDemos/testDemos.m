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

function testDemos()

    setSolver('sumproduct');

    global Dimple_TESTING_DEMOS;
    Dimple_TESTING_DEMOS = 1;

    demo_dir = '../../../../demo';
    my_current_directory = pwd;
    cd(demo_dir);

    %% 00_XorAndNot

    cd('00_XorAndNot');
    run;
    close;

    assertElementsAlmostEqual(w.Belief,0.375);
    assertElementsAlmostEqual(x.Belief,0.6250);
    assertElementsAlmostEqual(y.Belief,0.75);
    assertElementsAlmostEqual(z.Belief,0.75);

    %% 01_6BitCode

    cd(my_current_directory);

    load 6bitgolden;
    cd(demo_dir);
    cd('01_6BitCode');
    run;
    assertElementsAlmostEqual(d.Belief,x);

    
    %% 02_LDPC
    skipLDPC = false;
    if (isempty(ver('comm')))
        dtrace(true, 'WARNING: testDemos runSingleCodeword was skipped because communications system toolbox not installed');
        skipLDPC = true;
    else
    
        [hasLicense err] = license('checkout', 'communication_toolbox');
        if ~hasLicense
            dtrace(true, 'WARNING: testDemos runSingleCodeword was skipped because communications system toolbox license could not be obtained');
            skipLDPC = true;
        end
    end

    %Single codeword
    if(~skipLDPC)
        cd('../02_LDPC');
        runSingleCodeword;
        assertTrue(numMsgErrors == 0);    
    
        %BER plot
        run;
        close;
    end
    %% 03_SudokuExample
    cd('../03_SudokuExample');
    run;
    assertTrue(valid == 1);
    close;

    cd('../16_KalmanFilter');
    run;
    close;
    assertTrue(norm(fgxs-gxs) < 6.5);
    assertTrue(norm(fgys-gys) < 6);
    runUnivariate;
    close;
    assertTrue(norm(results-realZ) < 70);

    cd(my_current_directory);
    
end