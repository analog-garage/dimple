% testParameterizedMessage unit tests for ParameterizedMessage classes
%
% See also ParameterizedMessage, DiscreteMessage

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

function testParameterizedMessage()
    testDiscreteMessage();
end

function testDiscreteMessage()
    dm1 = DiscreteMessage([.4 .6]);
    assertDiscreteMessageInvariants(dm1);
    assertEqual([.4; .6], dm1.Weight);
    
    dm2 = DiscreteMessage(4);
    assertDiscreteMessageInvariants(dm2);
    assertEqual(ones(4,1), dm2(:));
    
    dm3 = DiscreteMessage([1 2], 'energy');
    assertDiscreteMessageInvariants(dm3);
    assertEqual([1 2]', dm3.Energy);
    
    dm4 = DiscreteMessage([2 3 4], 'ener');
    assertDiscreteMessageInvariants(dm4);
    assertEqual([2 3 4]', dm4.Energy);

    assertExceptionThrown(@() DiscreteMessage(), 'MATLAB:narginchk:notEnoughInputs');
    assertExceptionThrown(@() DiscreteMessage([1 2], 'wieght'), 'MATLAB:unrecognizedStringChoice');
    assertExceptionThrown(@() DiscreteMessage([1 NaN]), 'MATLAB:expectedNonNaN');
    assertExceptionThrown(@() DiscreteMessage([1 2; 3 4]), 'MATLAB:expectedVector');
    assertExceptionThrown(@() DiscreteMessage([]), 'MATLAB:expectedVector');
    assertExceptionThrown(@() DiscreteMessage(['foo', 'bar']), 'MATLAB:invalidType');
    assertExceptionThrown(@() DiscreteMessage(1), '', 'Size must be greater than one.');
end

function assertParameterizedMessageInvariants(message)
    assertTrue(isa(message, 'ParameterizedMessage'));
    
    assertTrue(isa(message.IParameters, ...
        'com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage'));
    assertEqual(message.IParameters, message.getProxyObject());
    assertEqual(message.IParameters, unwrapProxyObject(message));
end

function assertDiscreteMessageInvariants(message)
    assertParameterizedMessageInvariants(message);
    
    assertTrue(isa(message.IParameters, ...
        'com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage'));
    
    energy = message.Energy;
    weight = message.Weight;
    
    assertEqual(weight, message(:));
    assertEqual(weight(1), message(1));
    assertEqual(weight(1:2), message(1:2));
    assertElementsAlmostEqual(energy, -log(weight));
    assertEqual(numel(weight), message.end(1,2));
    
    message2 = DiscreteMessage(weight);
    assertEqual(weight, message2.Weight);
    message2(1) = 42;
    assertEqual(42, message2(1));
    assertEqual(42, message2.Weight(1));
    assertEqual(weight(2:end), message2(2:end));
    
    message3 = DiscreteMessage(message.IParameters);
    assertEqual(message.IParameters, message3.IParameters);
end

