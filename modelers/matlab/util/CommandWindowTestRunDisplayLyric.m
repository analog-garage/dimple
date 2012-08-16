%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2012 Analog Devices Inc.
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

classdef CommandWindowTestRunDisplayLyric < CommandWindowTestRunDisplay    
    properties (SetAccess = private)
        NumFailures = 0
        
        %NumErrors Number of test errors during execution
        NumErrors = 0
        
        %NumTestCases Total number of test cases executed
        NumTestCases = 0
        
    end
    
    methods
        
        function testCaseFailure(self, test_case, failure_exception)
            testCaseFailure@CommandWindowTestRunDisplay(self, test_case, failure_exception);
            self.NumFailures = self.NumFailures + 1;
        end
        
        function testCaseError(self, test_case, error_exception)
            %testCaseError Log test error information
            %    obj.testCaseError(test_case, error_exception) logs the test
            %    case error information.
            
            testCaseError@CommandWindowTestRunDisplay(self, test_case, failure_exception);
            self.NumErrors = self.NumErrors + 1;            
        end
        
        function faults = getFaults(self)
            faults = self.Faults;
        end
    end
end

