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
        Indices;
        VectorObject;
    end
    methods
        function obj = MatrixObject(vectorObject,indices)
            obj.Indices = indices;
            obj.VectorObject = vectorObject;
        end
        
        function varargout = subsref(obj,S)
            
            varargout = cell(1,max(1,nargout));
            switch S(1).type
                case '()'
                    %TODO: put in subroutine?
                    ind = obj.Indices(S(1).subs{:});
                    newobj = obj.createObjectFromReorderedIndices(ind);
                    
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
                tmp = subsref(obj,S(1));
                a = subsasgn(tmp,S(2:end),B);
            else
                switch S(1).type
                    case '.'
                        a = builtin('subsasgn',obj,S,B);
                    case '()'
                        if ~ isa(B,'MatrixObject')
                            error('must assign matrix objects');
                        end
                        obj.verifyCanConcatenate({B});
                        indices = obj.Indices(S(1).subs{:});
                        obj.VectorObject.replace(B.VectorObject,indices(:));
                    case '{}'
                        error('{} not supported');
                end
            end
        end
        
        function x = length(obj)
            x = length(obj.Indices);
        end
        
        function x = size(obj,varargin)
            x = size(obj.Indices,varargin{:});
        end
        
        %Cannot implement this as it messes up the builtin('subsref')
        %and builtin('subsasgn').  Would be nice to figure out a way to fix
        %this.
        %function x = numel(obj)
        %    x = numel(obj.Indices);
        %end
        
        function x = end(obj,k,n)
            if n == 1
                x = numel(obj.Indices);
            else
                x = size(obj.Indices,k);
            end
        end
        
        function var = repmat(a,varargin)
            indices = a.Indices;
            indices = repmat(indices,varargin{:});
            var = a.createObjectFromReorderedIndices(indices);
        end
        
        
        function retval = isequal(a,b)
            if ~isa(b,'MatrixObject')
                retval = false;
            else
                retval = isequal(a.Indices,b.Indices) && ...
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
            x = a.createObject(a.VectorObject,a.Indices');
        end
        
        function x = reshape(obj,varargin)
            indices = reshape(obj.Indices,varargin{:});
            x = obj.createObject(obj.VectorObject,indices);
        end
        
        function x = fliplr(obj)
            indices = fliplr(obj.Indices);
            x = obj.createObjectFromReorderedIndices(indices);
        end
        
        function x = flipud(obj)
            indices = flipud(obj.Indices);
            x = obj.createObjectFromReorderedIndices(indices);
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
        function x = createObjectFromReorderedIndices(obj,indices)
            varids = reshape(indices,numel(indices),1);
            vectorObject = obj.VectorObject.getSlice(varids);
            indices = reshape(0:(numel(varids)-1),size(indices));
            x = obj.createObject(vectorObject,indices);
        end
        
        function x = docat(obj,catmethod,varargin)
            
            obj.verifyCanConcatenate(varargin(2:end));
            
            indices_all = [];
            vector_object_indices_all = [];
            
            vectorObjects = cell(size(varargin));
            
            
            for i = 1:length(varargin)
                indices = varargin{i}.Indices;
                vectorObjects{i} = varargin{i}.VectorObject;
                vector_object_indices = ones(size(indices))*i-1;
                indices_all = catmethod(indices_all,indices);
                vector_object_indices_all = catmethod(...
                    vector_object_indices_all,vector_object_indices);
            end
            
            one_d_indices_all = reshape(indices_all,numel(indices_all),1);
            one_d_vector_object_indices_all = ...
                reshape(vector_object_indices_all,...
                numel(vector_object_indices_all),1);
            
            vectorObject = varargin{1}.VectorObject.concat(...
                vectorObjects,one_d_vector_object_indices_all,one_d_indices_all);
            
            indices = 0:numel(indices_all)-1;
            indices = reshape(indices,size(indices_all));
            
            x = obj.createObject(vectorObject,indices);
        end
    end
    
    methods (Access=protected)
        function v = unpack(obj,stuff)
            if size(stuff,1) ~= numel(obj.Indices)
                error('mismatch of sizes');
            end
            stuff = stuff(obj.Indices(:)+1,:);
            v = reshape(stuff,[size(obj.Indices) size(stuff,2)]);
        end
        
        function v = pack(obj,values)
            numValsPerObj = numel(values) / numel(obj.Indices);
            if mod(numValsPerObj,1) ~= 0
                error('invalid number of values');
            end
            
            v = reshape(values,numel(values)/numValsPerObj,numValsPerObj);
            v = v(obj.Indices+1,:);
        end
    end
    
    methods (Abstract, Access = protected)
        retval = createObject(obj,vectorObject,indices);
        verifyCanConcatenate(obj,otherObjects);
    end
    
    
end