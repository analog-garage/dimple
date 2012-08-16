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

classdef FactorGraph < handle
    properties(Access=public)
        TableFactory;
        Solver;
        Name;
        ExplicitName;
        QualifiedName;
        Label;
        QualifiedLabel;
        UUID;
        Energy;
        NumIterations;
        NestedGraphs;
        IGraph;
        Modeler;
        Factors;
        FactorsFlat;
        FactorsTop;
        NonGraphFactors;
        NonGraphFactorsFlat;
        NonGraphFactorsTop;
        Nodes;
        NodesFlat;
        NodesTop;
        Variables;
        VariablesFlat;
        VariablesTop;
        Schedule;
        Scheduler;
        AdjacencyMatrix;
        FactorGraphStreams;
        
    end
    methods
        function obj = FactorGraph(varargin)
            
            setFactorGraph(obj);
            if numel(varargin) == 2 && isequal(varargin{1},'nestedGraph')
                obj.IGraph = varargin{2};
                obj.TableFactory = FactorTableFactory();
            elseif numel(varargin) == 2 && isequal(varargin{1},'igraph')
                obj.IGraph = varargin{2};
                obj.TableFactory = FactorTableFactory();
            else
                
                modeler = getModeler();
                
                obj.TableFactory = FactorTableFactory();
                
                obj.Modeler = modeler;
                for i = 1:numel(varargin)
                    n = prod(size(varargin{i}));
                    varargin{i} = reshape(varargin{i},n,1);
                end
                if length(varargin) > 1
                    %variables = VariableBase.docat(@vertcat,varargin{:});
                    %varMat = variables.VarMat;
                    varMat = cell(size(varargin));
                    for i = 1:length(varargin)
                        varMat{i} = varargin{i}.VarMat;
                    end
                    
                elseif length(varargin) == 1
                    varMat = varargin{1}.VarMat;
                else
                    varMat = modeler.createVariableVector('',[],0);
                end
                
                obj.IGraph = modeler.createGraph(varMat);
            end
        end
        
        function iters = get.NumIterations(obj)
            iters = obj.Solver.getNumIterations();
        end
        
        function streams = get.FactorGraphStreams(obj)
            %TODO: wrap them
           streams = cell(obj.IGraph.getFactorGraphStreams()); 
           
           for i = 1:length(streams)
              streams{i} = FactorGraphStream(streams{i});
           end
        end
        
        function set.NumIterations(obj,iters)
            obj.Solver.setNumIterations(iters);
        end
        
        function initialize(obj)
            obj.IGraph.initialize();
        end
        
        
        function energy = get.Energy(obj)
            energy = obj.IGraph.getEnergy();
        end
        
        %createTable is kept around for legacy.
        function table = createTable(obj,indices,values,varargin)                                    
            table = FactorTable(indices,values,varargin{:});
        end
        
        
        function advance(obj)
           obj.IGraph.advance(); 
        end
        
        function reset(obj)
            obj.IGraph.reset();
        end
        
        function ret = hasNext(obj)
           ret = obj.IGraph.hasNext(); 
        end
        
        function factorStream = addRepeatedFactor(obj,factor,varargin)
            
            if isnumeric(varargin{1})
                bufferSize = varargin{1};
                varargin = varargin(2:end);
            else
                bufferSize = 1; 
            end

            
            args = cell(size(varargin));
            
            for i = 1:numel(varargin)
                %is either variable or variable stream
                if isa(varargin{i},'IVariableStreamSlice')
                   args{i} = varargin{i}.IVariableStreamSlice; 
                elseif isa(varargin{i},'VariableBase')
                    args{i} = varargin{i}.VarMat;
                else
                    error('not supported');
                end
            end
            
            pfs = obj.IGraph.addRepeatedFactor(factor.IGraph,bufferSize,args);
            factorStream = FactorGraphStream(pfs);
        end
        
        function retval = addFactorNoCache(obj,firstArg,varargin)
            retval = obj.addFactorWithCacheFlag(false,firstArg,varargin{:});
        end
        
        function retval = addFactor(obj,firstArg,varargin)
            retval = obj.addFactorWithCacheFlag(true,firstArg,varargin{:});
        end
        
        
        function str = getAdjacencyString(obj)
            str = obj.IGraph.getAdjacencyString();
        end
        function str = getNodeString(obj)
            str = obj.IGraph.getNodeString();
        end
        function str = getFullString(obj)
            str = obj.IGraph.getFullString();
        end
        
        function graphs = get.NestedGraphs(obj)
            igraphs = cell(obj.IGraph.getNestedGraphs());
            
            graphs = cell(numel(igraphs),1);
            
            for i = 1:numel(igraphs)
                graphs{i} = FactorGraph('igraph',igraphs{i});
            end
        end
        
        function retval = eq(a,b)
            retval = isequal(a,b);
        end
        
        function retval = isequal(a,b)
            if ~isa(b,'FactorGraph')
                retval = false;
            else
                retval = isequal(a.IGraph.getId(),b.IGraph.getId());
            end
        end
        
        
        %{
        Factors;
        FactorsFlat;
        FactorsTop;
        FactorsAndNestedGraphs;
        FactorsAndNestedGraphsTop;
        FactorsAndNestedGraphsFlat;
        getFactors(nestingLevel)
        getFactorsAndNestedGraphs(nestingLevel)
        
        Variables
        VariablesFlat
        VariablesTop
        getVariables(nestingLevel)
        %}
        
        function nodes = getNodes(obj,relativeNestingDepth,forceIncludeBoundaryVariables)
            if nargin < 3
                forceIncludeBoundaryVariables = false;
            end
            vars = obj.getVariables(relativeNestingDepth,forceIncludeBoundaryVariables);
            factors = obj.getFactors(relativeNestingDepth);
            
            nodes = [vars; factors];
        end
        
        function nodes = get.Nodes(obj)
            nodes = obj.NodesFlat;
        end
        
        function nodes = get.NodesFlat(obj)
            nodes = obj.getNodes(intmax);
        end
        
        function nodes = get.NodesTop(obj)
            nodes = obj.getNodes(0);
        end
        
        function variables = getVariables(obj,relativeNestingDepth,forceIncludeBoundaryVariables)
            
            if nargin < 3
                forceIncludeBoundaryVariables = false;
            end
            
            tmp = obj.IGraph.getVariableVector(relativeNestingDepth,forceIncludeBoundaryVariables);
            variables = cell(tmp.size(),1);
            
            for i = 1:tmp.size()
                var = tmp.getSlice(i-1);
                
                variables{i} = wrapProxyObject(var);
            end
        end
        
        function variables = get.Variables(obj)
            variables = obj.VariablesFlat;
        end
        
        function variables = get.VariablesFlat(obj)
            variables = obj.getVariables(intmax);
        end
        
        function variables = get.VariablesTop(obj)
            variables = obj.getVariables(0);
        end
        
        function factors = getNonGraphFactors(obj,relativeNestingDepth)
            tmp = cell(obj.IGraph.getNonGraphFactors(relativeNestingDepth));
            factors = cell(size(tmp));
            for i = 1:length(factors)
                factors{i} = wrapProxyObject(tmp{i});
            end
        end
        
        function factors = get.NonGraphFactors(obj)
            factors = obj.NonGraphFactorsFlat;
        end
        
        function factors = get.NonGraphFactorsFlat(obj)
            factors = obj.getNonGraphFactors(intmax);
        end
        
        function factors = get.NonGraphFactorsTop(obj)
            factors = obj.getNonGraphFactors(0);
        end
        
        function factors = getFactors(obj,relativeNestingDepth)
            tmp = cell(obj.IGraph.getFactors(relativeNestingDepth));
            factors = cell(size(tmp));
            for i = 1:length(factors)
                factors{i} = wrapProxyObject(tmp{i});
            end
        end
        
        function factors = get.Factors(obj)
            factors = obj.FactorsFlat;
        end
        
        function factors = get.FactorsFlat(obj)
            factors = obj.getFactors(intmax);
        end
        
        function factors = get.FactorsTop(obj)
            factors = obj.getFactors(0);
        end
        
        %{
function factors = get.Factors(obj)
            tmp = cell(obj.IGraph.getFactors());
            factors = cell(size(tmp));
            for i = 1:length(factors)
                factors{i} = obj.wrapProxyObject(tmp{i});
            end
            
        end
        %}
        
        function disp(obj)
            disp(obj.Label);
        end
        
        function ret = getFactorByName(obj, name)
            ret = [];
            tmp = obj.IGraph.getFactorByName(name);
            if tmp ~= 0
                if tmp.isDiscrete()
                    ret = DiscreteFactor(tmp);
                else
                    ret = Factor(tmp);
                end
            end
        end
        function ret = getFactorByUUID(obj, uuid)
            ret = [];
            tmp = obj.IGraph.getFactorByUUID(uuid);
            if tmp ~= 0
                if tmp.isDiscrete()
                    ret = DiscreteFactor(tmp);
                else
                    ret = Factor(tmp);
                end
            end
        end
        
        function ret = getVariableByName(obj, name)
            ret = [];
            tmp = obj.IGraph.getVariableVectorByName(name);
            if tmp ~= 0
                ret = Variable(tmp.getDomain().getElements(), 'existing', tmp, 0);
            end
        end
        function ret = getVariableByUUID(obj, uuid)
            ret = [];
            tmp = obj.IGraph.getVariableVectorByUUID(uuid);
            if tmp ~= 0
                ret = Variable(tmp.getDomain().getElements(), 'existing', tmp, 0);
            end
        end
        
        function ret = getGraphByName(obj, name)
            ret = [];
            tmp = obj.IGraph.getGraphByName(name);
            if tmp ~= 0
                ret = FactorGraph('nestedGraph', tmp);
            end
        end
        
        function ret = getGraphByUUID(obj, uuid)
            ret = [];
            tmp = obj.IGraph.getGraphByUUID(uuid);
            if tmp ~= 0
                ret = FactorGraph('nestedGraph', tmp);
            end
        end
        
        function istree = isTree(obj,relativeNestingDepth)
            if nargin < 2
                relativeNestingDepth = intmax;
            end
            
            istree = obj.IGraph.isTree(relativeNestingDepth);
        end
        
        function istree = isTreeTop(obj)
            istree =  obj.isTree(0);
        end
        
        function istree = isTreeFlat(obj)
            istree =  obj.isTree();
        end
        
        function nodes = depthFirstSearch(obj,node,searchDepth,relativeNestingDepth)
            if nargin < 4
                relativeNestingDepth = intmax;
            end
            if nargin < 3
                searchDepth = intmax;
            end
            if isa(node,'VariableBase')
                varmat = node.VarMat;
                if varmat.size() ~= 1
                    error('only support passing single variable for now');
                end
                inode = varmat.getVariable(0);
            elseif isa(node,'Factor')
                inode = node.IFactor;
            else
                error('Unrecognized type of first argument to depthFirstSearch.');
            end
            
            inodes = cell(obj.IGraph.depthFirstSearch(inode,searchDepth,relativeNestingDepth));
            
            nodes = cell(size(inodes));
            for i = 1:numel(inodes)
                nodes{i} = wrapProxyObject(inodes{i});
                
            end
        end
        
        function nodes = depthFirstSearchFlat(obj,node,searchDepth)
            nodes = obj.depthFirstSearch(node,searchDepth);
        end
        
        function nodes = depthFirstSearchTop(obj,node,searchDepth)
            nodes = obj.depthFirstSearch(node,searchDepth,0);
        end
        
        function solve(obj,initialize)
            
            if nargin < 2
                initialize = true;
            end
            try
                % Rather than directly calling the solver, try to start the
                % solver as a separate thread and then polls the solver to
                % wait for it to either complete or be interrupted by
                % CTRL-C.  To account for the possibility of being
                % interrupted, the onCleanup function terminateSolver makes
                % sure the solver thread is terminated.
                if (~obj.IGraph.getSolver().isSolverRunning())
                    c = onCleanup(@() obj.terminateSolver());
                    obj.IGraph.getSolver().startSolver(initialize);
                    while obj.IGraph.getSolver().isSolverRunning();
                        pause(0.01);
                    end
                else
                    error('Attempt to run solver while it is already running');
                end
            catch Err
                % For backward compatability with solvers that don't
                % support running in a separate thread
                if (strcmp(Err.identifier,'MATLAB:noSuchMethodOrField'))
                    obj.IGraph.solve(initialize);
                    %error('ack');
                else
                    throw(Err);
                end
                %TODO: keep the error being raised
            end
            
        end
        
        % Use the scheduler to create a schedule for this graph
        function setScheduler(obj, scheduler)
            obj.IGraph.setScheduler(scheduler);
        end
        
        function set.Scheduler(obj,scheduler)
            obj.setScheduler(scheduler);
        end
        
        function varOrFactor = getVariableOrFactor(obj,tmp)
            if isa(tmp,'VariableBase')
                varmat = tmp.VarMat;
                %methods(varmat)
                if varmat.size ~= 1
                    error('can only pass a single variable to a schedule for now');
                end
                
                var = varmat.getVariable(0);
                varOrFactor = var;
            elseif isa(tmp,'Factor')
                varOrFactor = tmp.IFactor;
            elseif isa(tmp,'FactorGraph')
                varOrFactor = tmp.IGraph;
            else
                error(['Unsupported type: ' class(tmp)]);
            end
        end
        
        function set.Schedule(obj,schedule)
            %Make sure schedule is a cell array
            
            %Convert variable vectors to variables?
            for i = 1:length(schedule)
                tmp = schedule{i};
                
                if isa(tmp,'cell')
                    %could be an edge
                    if length(tmp) ~= 2
                        error('expected a list of two elements if trying to specify an edge.');
                    end
                    schedule{i} = {obj.getVariableOrFactor(tmp{1}),obj.getVariableOrFactor(tmp{2})};
                else
                    schedule{i} = obj.getVariableOrFactor(tmp);
                end
            end
            
            obj.IGraph.setSchedule(schedule);
            
            %Pass down to FactorGraph
        end
        
        
        
        function result = join(obj,varargin)
            
            if (length(varargin) < 2)
                error('Need at least two elements for join call');
            end
            
            if isa(varargin{1},'Factor')
                result = obj.joinFactors(varargin{:});
            else
                result = obj.joinVariables(varargin{:});
            end
            
        end
        
        function [newvar,equalsFactor] = split(obj,variable,varargin)
            if (variable.VarMat.size() ~= 1)
                error('only support with one variable for now');
            end
            
            v = variable.VarMat.getVariable(0);
            ifactors = cell(size(varargin));
            
            for i = 1:numel(varargin)
                tmp = varargin{i};
                ifactors{i} = tmp.IFactor;
            end
            
            varmat = obj.IGraph.split(v,ifactors);
            
            newvar = variable.createVariable(variable.Domain,varmat,0);
            equalsFactor = newvar.Factors{1};
        end
        
        function [x,y] = plot(obj,varargin)
            
            %For legacy
            if length(varargin)==1 && isa(varargin{1},'double')
                varargin = {'labels',1};
            end
            
            nodesSet = 0;
            depthSet = 0;
            
            nesting = intmax;
            rootNode = [];
            depth = 0;
            nodes = [];
            colorMap = containers.Map();
            defaultColor = [];
            uselabels = [];
            index = 1;
            test = 0;
            
            while index <= length(varargin)
                switch varargin{index}
                    case 'nodes'
                        if nodesSet
                            error('Cannot specify nodes twice');
                        elseif depthSet
                            error ('Cannot specify both depth and nodes');
                        end
                        
                        nodes = varargin{index+1};
                        
                        %Make sure there are no Variable vectors with more than one
                        %element
                        nodes = obj.flattenNodes(nodes);
                        
                        index = index+2;
                        nodesSet = 1;
                        
                    case 'depth'
                        if nodesSet
                            error ('Cannot specify both depth and nodes');
                        elseif depthSet
                            error('Cannot specify depth twice');
                        end
                        depthSet = 1;
                        rootNode = varargin{index+1};
                        depth = varargin{index+2};
                        index = index+3;
                    case 'nesting'
                        nesting = varargin{index+1};
                        index = index+2;
                    case 'color'
                        firstArg = varargin{index+1};
                        
                        if isa(firstArg,'char')
                            %set default color
                            defaultColor = firstArg;
                            index = index+2;
                            
                        elseif isa(firstArg,'cell')
                            %set a bunch of colors
                            secondArg = varargin{index+2};
                            
                            for i = 1:length(firstArg)
                                if colorMap.isKey(firstArg{i}.Name)
                                    error('color already defined for node');
                                end
                                colorMap(firstArg{i}.Name) = secondArg{i};
                            end
                            
                            index = index+3;
                            
                        else
                            %set a single color
                            secondArg = varargin{index+2};
                            if colorMap.isKey(firstArg.Name)
                                error('color already defined for node');
                            end
                            
                            colorMap(firstArg.Name) = secondArg;
                            
                            index = index+3;
                        end
                        
                    case 'labels'
                        uselabels = varargin{index+1};
                        index = index+2;
                    case 'test'
                        test = 1;
                        index = index+1;
                    otherwise
                        error('unsupported option %s',varargin{index});
                end
            end
            
            
            %might need to do depth first search
            if depthSet
                nodes = obj.depthFirstSearch(rootNode,depth,nesting);
            end
            
            if isempty(nodes)
                nodes = [obj.getVariables(nesting,true); obj.getFactors(nesting)];
            end
            
            if isempty(uselabels)
                uselabels = 0;
            end
            
            if isempty(defaultColor)
                if uselabels
                    defaultColor = 'w';
                else
                    defaultColor = 'b';
                end
            end
            
            %TODO: I should modify the getAdjacencyMatrix to eliminate
            %duplicate nodes and then possibly return the nodes of
            %interest?
            
            %first, we get the adjacency matrix
            A = obj.getAdjacencyMatrix(nodes);
            
            %next, we get the labels for the nodes
            labels = cell(size(nodes));
            for i = 1:length(labels)
                labels{i} = nodes{i}.Label;
            end
            
            %now we get the shapes
            node_shapes = zeros(size(nodes));
            for i = 1:length(nodes)
                if isa(nodes{i},'Factor') || isa(nodes{i},'FactorGraph')
                    node_shapes(i) = 1;
                else
                    node_shapes(i) = 0;
                end
            end
            
            if uselabels
                
                
                %now we print with labels and retrieve the locations
                
                if test
                    [x,y] = make_layout(A);
                    
                else
                    if ~ishold()
                        clf;
                    end
                    [x,y,h] = graph_draw(A,'node_labels',labels,'node_shapes',node_shapes); %,'linecolor','r');
                    
                    for i = 1:length(nodes)
                        if colorMap.isKey(nodes{i}.Name)
                            set(h(i,2),'facecolor',colorMap(nodes{i}.Name));
                        else
                            set(h(i,2),'facecolor',defaultColor);
                        end
                    end
                    
                end
            else
                [x,y] = make_layout(A);
                
                if ~test
                    savedhold = ishold();
                    
                    gplot(A,[x' y'],'-');
                    
                    hold on;
                    
                    for i = 1:length(nodes)
                        
                        if isa(nodes{i},'Factor')
                            shape = 's';
                        elseif isa(nodes{i},'FactorGraph')
                            shape='^';
                        else
                            %figre out if this is a boundary variable
                            if ~obj.isAncestorOf(nodes{i})
                                shape='*';
                            else
                                shape = 'o';
                            end
                        end
                        
                        if colorMap.isKey(nodes{i}.Name)
                            c = colorMap(nodes{i}.Name);
                            %gplot(
                            %set(h(i,2),'facecolor',colorMap(nodes{i}.Name));
                        else
                            c = defaultColor;
                            %set(h(i,2),'facecolor',color);
                        end
                        tmp = sprintf('%s%s',c,shape);
                        plot(x(i),y(i),tmp,'MarkerFaceColor',c);
                    end
                    
                    if ~savedhold
                        hold off;
                    end
                end
                
            end
            
            
        end
        
        
        function A = get.AdjacencyMatrix(obj)
            A = obj.getAdjacencyMatrix();
        end
        
        function [A,labels] = getAdjacencyMatrix(obj,arg,forceIncludeBoundaryVariables)
            
            if nargin < 3
                forceIncludeBoundaryVariables = true;
            end
            
            if nargin < 2
                [A,labels] = obj.getAdjacencyMatrixNodes();
            else
                if isnumeric(arg)
                    nodes = obj.getNodes(arg,forceIncludeBoundaryVariables);
                    [A,labels] = obj.getAdjacencyMatrix(nodes);
                    
                else
                    [A,labels] = obj.getAdjacencyMatrixNodes(arg);
                end
            end
            
        end
        
        function [A,labels] = getAdjacencyMatrixTop(obj)
            [A,labels] = obj.getAdjacencyMatrix(0);
        end
        
        function [A,labels] = getAdjacencyMatrixFlat(obj)
            [A,labels] = obj.getAdjacencyMatrix(intmax);
        end
        
        function ancestor = isAncestorOf(obj,node)
            pnode = [];
            if isa(node,'FactorGraph')
                pnode = node.IGraph;
            elseif isa(node,'Factor')
                pnode = IFactor;
            else
                varmat = node.VarMat;
                if varmat.size() ~= 1
                    error('not supported for variable vectors');
                end
                pnode = varmat.getVariable(0);
            end
            
            ancestor = obj.IGraph.isAncestorOf(pnode);
        end
        
        function setSolver(obj,solver,varargin)
            if ischar(solver)
                registry = getSolverRegistry();
                solver = registry.get(solver);
                solver = solver(varargin{:});
            end
            
            obj.IGraph.setSolver(solver);
        end
        
        function set.Solver(obj,solver)
            %obj.IGraph.setSolver(solver);
            obj.setSolver(solver);
        end
        
        function ret = get.Solver(obj)
            ret = obj.IGraph.getSolver();
        end
        
        function removeFactor(obj,factor)
            obj.IGraph.removeFactor(factor.IFactor);
        end
        
        function FileName = serializeToXML(obj, FileName, DirectoryName)
            FileName = obj.IGraph.serializeToXML(FileName, DirectoryName);
        end
        
        function name = get.Name(obj)
            name = char(obj.IGraph.getName());
        end
        
        function name = get.ExplicitName(obj)
            name = char(obj.IGraph.getExplicitName());
        end
        
        function name = get.QualifiedName(obj)
            name = char(obj.IGraph.getQualifiedName());
        end
        
        function name = get.Label(obj)
            name = char(obj.IGraph.getLabel());
        end
        function name = get.QualifiedLabel(obj)
            name = char(obj.IGraph.getQualifiedLabel());
        end
        function uuid = get.UUID(obj)
            uuid = obj.IGraph.getUUID();
        end
        
        function set.Name(obj,name)
            obj.IGraph.setName(name);
        end
        
        function set.Label(obj,name)
            obj.IGraph.setLabel(name);
        end
    end
    
    methods (Access = private)
        

        function retval = addFactorWithCacheFlag(obj,doCache,firstArg,varargin)
            
            %scan arguments and, if any are streams, call addRepeatedFactor
            requiresRepeated = false;
            for i = 1:length(varargin)
               if isa(varargin{i},'IVariableStreamSlice')
                   requiresRepeated = true;
                   break;
               end                   
            end
            
            if requiresRepeated
                retval = obj.addRepeatedFactor(firstArg,varargin{:});
            else
            

                %Should return objects.  Eitehr a Factor or a Factor Graph
                retval = [];

                if isa(firstArg,'function_handle')
                    retval = obj.addFunctionHandle(doCache,firstArg,varargin{:});
                elseif isa(firstArg,'FactorGraph')
                    retval = obj.addGraph(firstArg,varargin{:});
                elseif isa(firstArg,'FactorTable')
                    retval = obj.addTable(firstArg,varargin{:});
                elseif isa(firstArg,'double')
                    if numel(varargin) < 1
                        error('When adding a table with addFactor, user should specify both indices and values');
                    end
                    
                    if isa(varargin{1},'double')
                        if numel(varargin) < 2
                            error('need at least one variable');
                        end
                        retval = obj.addTableFromIndicesAndValues(firstArg,varargin{1},{varargin{2:end}});
                    else
                        retval = obj.addTableFromValues(firstArg,varargin);
                        
                    end                    
                    
                    %TODO: replace with a function call isFactorFunction
                elseif (isa(firstArg, 'com.analog.lyric.dimple.FactorFunctions.core.FactorFunction'))
                    retval = obj.addJavaFactorFunction(firstArg,varargin{:});
                else
                    error(['Unsupported type: ' class(firstArg)]);
                end
            end
        end
                
        
        %getAdjacencyMatrix(nodes)
        function [A,labels] = getAdjacencyMatrixNodes(obj,nodes)
            
            %TODO: boundary variables?
            
            if nargin < 2
                factors = obj.Factors;
                vars = obj.getVariables(intmax,true);
                
                nodes = [vars; factors];
            end
            
            nodes = obj.flattenNodes(nodes);
            
            labels = cell(size(nodes));
            for i = 1:length(labels)
                labels{i} = nodes{i}.Label;
            end
            
            
            for i = 1:length(nodes)
                if isa(nodes{i},'Factor')
                    nodes{i} = nodes{i}.IFactor;
                elseif isa(nodes{i},'FactorGraph')
                    nodes{i} = nodes{i}.IGraph;
                else
                    nodes{i} = nodes{i}.VarMat;
                end
            end
            
            
            A = double(obj.IGraph.getAdjacencyMatrix(nodes));
            
        end
        
        
        function flatCell = flattenNodes(obj,nodes)
            num = 0;
            for i = 1:length(nodes)
                if ~isa(nodes{i},'Factor') && ~isa(nodes{i},'FactorGraph');
                    num = num+nodes{i}.VarMat.size();
                else
                    num = num+1;
                end
                
            end
            
            flatCell = cell(num,1);
            index = 1;
            for i = 1:length(nodes)
                if ~isa(nodes{i},'Factor') && ~isa(nodes{i},'FactorGraph');
                    for j = 1:nodes{i}.VarMat.size()
                        flatCell{index} = wrapProxyObject(nodes{i}.VarMat.getSlice(j-1));
                        index = index+1;
                    end
                else
                    flatCell{index} = nodes{i};
                    index = index+1;
                end
            end
            
            
        end
        
        
        function factor = joinFactors(obj,varargin)
            
            
            
            factors = cell(length(varargin),1);
            
            for i = 1:length(varargin)
                factors{i} = varargin{i}.IFactor;
            end
            
            
            factor = obj.IGraph.joinFactors(factors);
            if factor.isDiscrete()
                factor = DiscreteFactor(factor);
            else
                factor = Factor(factor);
            end
        end
        
        function var = joinVariables(obj,varargin)
            
            %convert oldvars to list of variables
            vars = cell(length(varargin),1);
            for i = 1:length(varargin)
                if varargin{i}.VarMat.size() ~= 1
                    error('vectors not supported');
                end
                tmp = varargin{i}.VarMat.getVariable(0);
                vars{i} = tmp;
            end
            
            %call join on igraph
            tmpVar = obj.IGraph.joinVariables(vars);
            
            %TODO: figure out domain differently
            if tmpVar.isDiscrete()
                var = Discrete(cell(tmpVar.getVariable(0).getDomain().getElements()),'existing',tmpVar,0);
            else
                error('not yet supported');
                %var = Real(tmpVar.getDomain(),'existing',tmpVar,1);
            end
            %create new variable
        end
        
        function retval = addGraph(parentGraph,childGraph,varargin)
            
            varMat = parentGraph.getVarVector(varargin{:});
            retval = FactorGraph('nestedGraph',parentGraph.IGraph.addGraph(childGraph.IGraph,varMat));
        end
        
        function retval = addTable(obj,table,varargin)
            
            varMat = obj.getVarVector(varargin{:});
            
            %Wrap it in a factor function
            domains = {};
            
            index = 1;
            for i = 1:length(varargin)
                numVars = prod(size(varargin{i}));
                
                domain = varargin{i}.Domain.IDomain;
                
                for j = 1:numVars
                    
                    
                    domains{index} = domain;
                    index = index+1;
                end
                
            end
            
            
            func = obj.IGraph.createFactor(table.ITable,varMat);
            retval = DiscreteFactor(func);
        end
        
        
        function retval = addFunctionHandle(obj,doCache,funcHandle,varargin)
            
            %Modify this function to look at the class of funcHandle
            %Do something different based on function_handle, double, or
            %FactorGraph
            retval = [];
            funcName = func2str(funcHandle);
            customFuncExists = obj.IGraph.customFactorExists(funcName);
            %customFuncExists = obj.IGraph.getSolver().customFactorExists(obj.IGraph,funcName);
            
            if customFuncExists
                
                
                %TODO: this should be shared with non custom func code?
                
                for i = 1:length(varargin)
                    if isa(varargin{i},'VariableBase')
                        varargin{i} = varargin{i}.VarMat;
                    end
                end
                
                retval = Factor(obj.IGraph.createCustomFactor(funcName,varargin));
                
            else
                
                %make sure this is a discrete factor
                varMat = [];
                domains = cell(length(varargin),1);
                constants = zeros(length(varargin),1);
                
                curIndex = 1;
                
                %iterate through all the variables
                for i = 1:length(varargin)
                    
                    %if this is not a variable, it's a constant, so we deal
                    %with it differently
                    if (~isa(varargin{i},'VariableBase'))
                        %tmpVar = Variable({varargin{i}});
                        %varargin{i} = tmpVar;
                        constants(i) = 1;
                        
                        %i indexes the csl argument to addFactor
                        %j indexes the entry into a VarMat.  Because this
                        %is a constant, we only have one variable
                        domains{i} = {DiscreteDomain({varargin{i}})};
                        
                    else
                        %if we get here, this is in fact a variable
                        
                        %allocate enough domains for each element in the
                        %varmat
                        domains{i} = cell(size(varargin{i}.Indices));
                        
                        
                        ind = varargin{i}.Indices;
                        
                        %for each entry in the indices
                        for j = 1:numel(ind)
                            
                            %retrieve the domain
                            domain = varargin{i}.Domain;
                            
                            %error if this is not discrete
                            if ~domain.isDiscrete()
                                error('only discrete domains supported when adding function handle');
                            end
                            
                            %for each domain element, check that it is not
                            %a vector.
                            for k = 1:numel(domain.Elements)
                                if (numel(domain.Elements{k}) > 1) && (j > 1)
                                    msg = ['Dimple does not currently support passing ' ...
                                        'a variable vector to addFactor if that ' ...
                                        'variable vector''s domain contains vectors'];
                                    error(msg);
                                end
                            end
                            
                            
                            %varid = ind(j);
                            %args{curIndex} = varid;
                            
                            %store the domain element
                            domains{i}{j} = domain;
                        end
                    end
                    
                    
                    curIndex = curIndex+1;
                    
                    if ~constants(i)
                        if isempty(varMat)
                            varMat = varargin{i}.VarMat;
                        else
                            indices = [0:varMat.size()-1 0:varargin{i}.VarMat.size()-1];
                            varMatIndices = [zeros(1,varMat.size()) ones(1,varargin{i}.VarMat.size())];
                            varMat = varMat.concat({varMat varargin{i}.VarMat},varMatIndices,indices);
                        end
                    end
                    
                end
                
                if doCache
                    table = obj.TableFactory.getTable(funcHandle,domains,constants);
                else
                    table = FunctionEntry.createFactorTable(func2str(funcHandle),...
                    domains,constants,funcHandle);
                end
                
                retval = DiscreteFactor(obj.IGraph.createFactor(table{3},varMat));
            end
        end
        
        
        function retval = addJavaFactorFunction(obj,factorFunction,varargin)
            
            %TODO: this should be shared with code above?
            %vars = cell(length(varargin));
            for i = 1:length(varargin)
                if isa(varargin{i},'VariableBase')
                    varargin{i} = varargin{i}.VarMat;
                end
            end
            
            
            retval = Factor(obj.IGraph.createFactor(factorFunction,varargin));
        end
        
        
        function [varMat] = getVarVector(obj,varargin)
            
            varMat = [];
            
            for i = 1:length(varargin)
                if i == 1
                    varMat = varargin{i}.VarMat;
                else
                    varMat = varMat.concat({varMat varargin{i}.VarMat});
                end
            end
            
        end
        

        function retval = addTableFromIndicesAndValues(obj,indices,values,variables)
            domains = obj.getDomainsFromVariableList(variables);
            table = FactorTable(indices,values,domains{:});
            retval = obj.addTable(table,variables{:});            
        end
        
        function retval = addTableFromValues(obj,values,variables)
            domains = obj.getDomainsFromVariableList(variables);
            table = FactorTable(values,domains{:});
            retval = obj.addTable(table,variables{:});            
        end
        
        function domains  = getDomainsFromVariableList(obj,vars)

            %TODO: make common code for this
            domains = {};
            index = 1;
            for i = 1:length(vars)
                numVars = prod(size(vars{i}));

                for j = 1:numVars
                    domains{index} = vars{i}.Domain;
                    index = index+1;
                end
            end


        end        
        
        % This function is necessary because the solver runs in a separate
        % thread and so if the MATLAB is interrupted, the thread must be
        % interrupted.  This function is also called when the solver
        % terminates normally, so it only interrupts the solver if it is
        % still running.
        function terminateSolver(obj)
            if obj.IGraph.getSolver().isSolverRunning();
                obj.IGraph.getSolver().interruptSolver();
            end
        end
    end
end
