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

function string = dtrace(bool, varargin)
% dtrace - Simple debug trace output.
%
%   Signature:
%       string = dtrace(bool, varargin)
%
%   Usage:
%       If BOOL is true, the parameters specified by varargin are given to
%       sprintf, with the first parameter serving as the format string. The
%       resulting string is then written to stdout. If BOOL is false, no
%       nothing is written to stdout. The formatted string is always
%       returned.

    % If no parameters were passed, assume an empty string.
    if isempty(varargin)
        varargin = {''};
    end
    
    % If the last parameter is '...', neglect the terminating newline.
    if ischar(varargin{end}) && strcmp(varargin{end}, '...')
        newline = '';
        varargin = varargin(1:end-1);
    else
        newline = sprintf('\n');
    end
    
    % Get the formatted string.
    string = sprintf(varargin{:});

    if bool
        % Use fwrite() to write output to stdout. This avoids problems with
        % a function like fprintf() attempt to reformat the output.
        fwrite(1, [string newline]);
    end
end
