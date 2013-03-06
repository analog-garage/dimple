% The MatrixObject class provides an implementation of an object that can
% be treated like a MATLAB matrix.

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

classdef MatrixObject < handle
    properties (Access=public)
        VectorIndices;
        VectorObject;
    end
    methods
        function obj = MatrixObject(vectorObject,VectorIndices)
            obj.VectorIndices = VectorIndices;
            obj.VectorObject = vectorObject;
        end
        
        function varargout = subsref(obj,S)
            
            varargout = cell(1,max(1,nargout));
            switch S(1).type
                case '()'
                    %TODO: put in subroutine?
                    ind = obj.VectorIndices(S(1).subs{:});
                    newobj = obj.createObjectFromReorderedVectorIndices(ind);
                    
                    if (length(S) > 1)
                        [varargout{:}] = subsref(newobj,S(2:end));
                    else
                        varargout = {newobj};
                    end
                    
                case '.'
                    
                    %It would be nice if this could be dealt with more
                    %elegantly.
                    try
                        [varargout{:}] = builtin('subsref',obj,S);
                    catch e
                        if isequal(e.identifier,'MATLAB:unassignedOutputs')
                        elseif isequal(e.identifier,'MATLAB:maxlhs')
                            builtin('subsref',obj,S);
                        else
                            rethrow(e);
                        end
                    end
                case '{}'
                    error('brackets are not supported');
            end
        end
        
        function obj = subsasgn(obj,S,B)
            
            if numel(S) > 1
                
                %This is needed to differentiate between properties and
                %function calls.
                if isequal(S(2).type,'()')
                    firstArgs = S(1:2);
                    secondArgs = S(3:end);
                else
                    firstArgs = S(1);
                    secondArgs = S(2:end);
                end
                
                tmp = subsref(obj,firstArgs);
                a = subsasgn(tmp,secondArgs,B);
            else
                switch S(1).type
                    case '.'
                        a = builtin('subsasgn',obj,S,B);
                    case '()'
                        if ~ isa(B,'MatrixObject')
                            error('must assign matrix objects');
                        end
                        obj.verifyCanConcatenate({B});
                        VectorIndices = obj.VectorIndices(S(1).subs{:});
                        obj.VectorObject.replace(B.VectorObject,VectorIndices(:));
                    case '{}'
                        error('{} not supported');
                end
            end
        end
        
        function x = length(obj)
            x = length(obj.VectorIndices);
        end
        
        function x = size(obj,varargin)
            x = size(obj.VectorIndices,varargin{:});
        end
        
        %Cannot implement this as it messes up the builtin('subsref')
        %and builtin('subsasgn').  Would be nice to figure out a way to fix
        %this.
        %function x = numel(obj)
        %    x = numel(obj.VectorIndices);
        %end
        
        function x = end(obj,k,n)
            if n == 1
                x = numel(obj.VectorIndices);
            else
                x = size(obj.VectorIndices,k);
            end
        end
        
        function var = repmat(a,varargin)
            VectorIndices = a.VectorIndices;
            VectorIndices = repmat(VectorIndices,varargin{:});
            var = a.createObjectFromReorderedVectorIndices(VectorIndices);
        end
        
        
        function retval = isequal(a,b)
            if ~isa(b,'MatrixObject')
                retval = false;
            else
                retval = isequal(a.VectorIndices,b.VectorIndices) && ...
                    isequal(a.VectorObject.getIds(),b.VectorObject.getIds());
            end
        end
        
        function retval = eq(a,b)
            retval = a.isequal(b);
        end
        
        function x = ctranspose(a)
            x = a.transpose();
        end
        function x = transpose(a)
            x = a.createObject(a.VectorObject,a.VectorIndices');
        end
        
        function x = reshape(obj,varargin)
            VectorIndices = reshape(obj.VectorIndices,varargin{:});
            x = obj.createObject(obj.VectorObject,VectorIndices);
        end
        
        function x = fliplr(obj)
            VectorIndices = fliplr(obj.VectorIndices);
            x = obj.createObjectFromReorderedVectorIndices(VectorIndices);
        end
        
        function x = flipud(obj)
            VectorIndices = flipud(obj.VectorIndices);
            x = obj.createObjectFromReorderedVectorIndices(VectorIndices);
        end
        
        function x = horzcat(varargin)
            x = varargin{1}.docat(@horzcat,varargin{:});
        end
        
        function x = vertcat(varargin)
            x = varargin{1}.docat(@vertcat,varargin{:});
        end
        
        
    end
    
    methods(Access=private)
        
        %Private
        function x = createObjectFromReorderedVectorIndices(obj,VectorIndices)
            varids = reshape(VectorIndices,numel(VectorIndices),1);
            vectorObject = obj.VectorObject.getSlice(varids);
            VectorIndices = reshape(0:(numel(varids)-1),size(VectorIndices));
            x = obj.createObject(vectorObject,VectorIndices);
        end
        
        function x = docat(obj,catmethod,varargin)
            
            obj.verifyCanConcatenate(varargin(2:end));
            
            VectorIndices_all = [];
            vector_object_VectorIndices_all = [];
            
            vectorObjects = cell(size(varargin));
            
            
            for i = 1:length(varargin)
                VectorIndices = varargin{i}.VectorIndices;
                vectorObjects{i} = varargin{i}.VectorObject;
                vector_object_VectorIndices = ones(size(VectorIndices))*i-1;
                VectorIndices_all = catmethod(VectorIndices_all,VectorIndices);
                vector_object_VectorIndices_all = catmethod(...
                    vector_object_VectorIndices_all,vector_object_VectorIndices);
            end
            
            one_d_VectorIndices_all = reshape(VectorIndices_all,numel(VectorIndices_all),1);
            one_d_vector_object_VectorIndices_all = ...
                reshape(vector_object_VectorIndices_all,...
                numel(vector_object_VectorIndices_all),1);
            
            vectorObject = varargin{1}.VectorObject.concat(...
                vectorObjects,one_d_vector_object_VectorIndices_all,one_d_VectorIndices_all);
            
            VectorIndices = 0:numel(VectorIndices_all)-1;
            VectorIndices = reshape(VectorIndices,size(VectorIndices_all));
            
            x = obj.createObject(vectorObject,VectorIndices);
        end
        
        
    end
    
    methods (Access=protected)
        
    end
    
    methods (Abstract, Access = protected)
        retval = createObject(obj,vectorObject,VectorIndices);
        verifyCanConcatenate(obj,otherObjects);
    end
    
    methods (Static)
        function v = unpack(stuff,indices,returnSingletonIfOnlyOne)
            % Routine to convert two dimensional arrays returned by
            % VectorObjects into multidimensional arrays that relate to the
            % VectorIndices.
            %
            % Arguments:
            % stuff - A one or two dimensional array of stuff to be
            % unpacked.
            % indices - The indices of the MatrixObject.  The indices
            % determine how the stuff is unpacked.
            % returnSingletonIfOnlyOne - In the case of cell arrays, this
            % flag will return the object itself rather than an array of
            % one object if there is only one object.

            if nargin < 3
                returnSingletonIfOnlyOne = false;
            end
            
            if iscell(stuff)
                if numel(indices) == 1
                    v = reshape(stuff,numel(stuff),1);
                    if returnSingletonIfOnlyOne
                        v = v{1};
                    end
                else
                    if size(stuff,1) ~= numel(indices)
                        error('mismatch of sizes');
                    end
                    stuff = stuff(indices(:)+1,:);
                    v = reshape(stuff,size(indices));
                    if numel(v) == 1 && returnSingletonIfOnlyOne
                        v = v{1};
                    end
                end
                
            else
                
                if numel(indices) == 1
                    %v = reshape(stuff,numel(stuff),1);
                    sz = size(stuff);                    
                    sz = sz(2:end);
                    if length(sz) < 2
                        sz = [sz 1];
                    end
                    v = reshape(stuff,sz);
                else
                    if size(stuff,1) ~= numel(indices)
                        error('mismatch of sizes');
                    end
                    stuffsz = size(stuff);
                    stuff = stuff(indices(:)+1,:);
                    sz = size(indices);
                    if sz(length(sz)) == 1
                        v = reshape(stuff,[sz(1:end-1) stuffsz(2:end)]);
                    else
                        v = reshape(stuff,[size(indices) stuffsz(2:end)]);
                    end
                end
            end
        end
        
        function v = pack(values,indices)
            % Routine to pack MATLAB arguments into a form that can be
            % passed to the VectorObject
            %
            % Expects as input a multidimensional vector corresponding to
            % the dimensions of a MatrixObject.  There can optionally be
            % one extra dimension for an array of objects to be passed to
            % the underlying object.
            %
            % This routine returns values as a one or two dimensional
            % vector with dimensions: NumObjects x NumValuesPerObject
            % It does not need to return indices because the routine itself
            % reorders the two dimensional array according to linear
            % indices of the underlying VectorObject.
            %
            % TODO: should deal with cell arrays if the values cannot be
            % turned into a matrix
            
            numValsPerObj = prod(size(values)) / numel(indices);
            if mod(numValsPerObj,1) ~= 0
                error('invalid number of values');
            end
            
            v = reshape(values,prod(size(values))/numValsPerObj,numValsPerObj);
            v = v(indices+1,:);
            
            %figure out hte dimensions of the remaining values
            vsz = size(values);
            tmp = numValsPerObj;
            i = length(vsz)+1;
            while tmp > 1
                i = i-1;
                tmp =  tmp/vsz(i);
            end
            
            if i <= length(vsz)
                newsize = vsz(i:end);
                v = reshape(v, [size(v,1) newsize]);
            end

        end
    end
    
end
