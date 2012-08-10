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

