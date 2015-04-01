classdef FactorGraph < Node
    % The FactorGraph class represents a collection of variables and the
    % factors that relate these variables to one another.  Users create new
    % factors by calling addFactor, set inputs on the variables, and solve
    % using the FactorGraph.solve method.
    %
    % FactorGraph properties:
    %    Solver - Retrieves the underlying solver object.
    %    Name - Can be used to set/get the name of the FactorGraph.
    %    Label - Used when plotting the graph.
    %    NumIterations - Sets the nuber of iterations on the solver.  This
    %                    property has a solver specific meaning.
    %    Factors - Returns the list of Factors associated with this graph.
    %    Variables - Returns the list of Variables associated with this graph.
    %
    % FactorGraph Methods
    %    solve - Calls the solve method on the underlying solver.
    %    plot - Plots the graph.
    %    addFactor - Associates one or more variables with a Factor.
    
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    
    
    properties(Access=public)
        TableFactory;
        %Set/get the number of iterations of the underlying solver.
        NumIterations;
        NestedGraphs;
        %Gets the Factors created by calling addFactor on the graph.
        Factors;
        FactorsFlat;
        FactorsTop;
        Modeler;
        NonGraphFactors;
        NonGraphFactorsFlat;
        NonGraphFactorsTop;
        Nodes;
        NodesFlat;
        NodesTop;
        %Gets all of the variables that are associated with the graph
        %through addFactor.
        Variables;
        VariablesFlat;
        VariablesTop;
        Schedule;
        Scheduler;
        AdjacencyMatrix;
        BetheFreeEnergy;
        FactorGraphStreams;
        NumSteps;
    end
    methods
        function obj = FactorGraph(varargin)
            obj@Node([],[]);
            
            if numel(varargin) == 2 && isequal(varargin{1},'nestedGraph')
                obj.VectorObject = varargin{2};
                obj.TableFactory = FactorTableFactory();
                obj.VectorIndices = 0:(obj.VectorObject.size()-1);
            elseif numel(varargin) == 2 && isequal(varargin{1},'VectorObject')
                obj.VectorObject = varargin{2};
                obj.TableFactory = FactorTableFactory();
                obj.VectorIndices = 0:(obj.VectorObject.size()-1);
            elseif numel(varargin) == 3 && isequal(varargin{1},'VectorObject')
                obj.VectorObject = varargin{2};
                obj.TableFactory = FactorTableFactory();
                obj.VectorIndices = varargin{3};
            else
                % Set the default graph (only in this case where the graph
                % is created explicitly by the user rather than as a side effect)
                setFactorGraph(obj);

                modeler = getModeler();
                
                obj.TableFactory = FactorTableFactory();
                
                obj.Modeler = modeler;
                for i = 1:numel(varargin)
                    n = prod(size(varargin{i}));
                    varargin{i} = reshape(varargin{i},n,1);
                end
                if length(varargin) > 1
                    %variables = VariableBase.docat(@vertcat,varargin{:});
                    %VectorObject = variables.VectorObject;
                    VectorObject = cell(size(varargin));
                    for i = 1:length(varargin)
                        VectorObject{i} = varargin{i}.VectorObject;
                    end
                    
                elseif length(varargin) == 1
                    VectorObject = varargin{1}.VectorObject;
                else
                    VectorObject = modeler.createVariableVector('',[],0);
                end
                
                obj.VectorObject = modeler.createGraph(VectorObject);
                obj.VectorIndices = 0:(obj.VectorObject.size()-1);
            end
        end
        
        function bfe = get.BetheFreeEnergy(obj)
            bfe = obj.VectorObject.getBetheFreeEnergy();
        end
        
        function iters = get.NumIterations(obj)
            iters = obj.getOption('BPOptions.iterations');
        end
        
        function streams = get.FactorGraphStreams(obj)
            streams = cell(obj.VectorObject.getFactorGraphStreams());
            
            for i = 1:length(streams)
                streams{i} = FactorGraphStream(streams{i});
            end
        end
        
        function set.NumIterations(obj,iters)
            obj.setOption('BPOptions.iterations', iters);
        end
        
        function set.NumSteps(obj,steps)
            if isinf(steps)
                obj.VectorObject.setNumStepsInfinite(true);
            else
                obj.VectorObject.setNumStepsInfinite(false);
                obj.VectorObject.setNumSteps(steps);
            end
           
        end
        
        function ret = get.NumSteps(obj)
            ret = obj.VectorObject.getNumSteps();
        end
        
        function initialize(obj)
            obj.VectorObject.initialize();
        end               
        
        %createTable is kept around for legacy.
        function table = createTable(obj,VectorIndices,values,varargin)
            table = FactorTable(VectorIndices,values,varargin{:});
        end
        
        function addBoundaryVariables(obj,varargin)
           input = cell(size(varargin));
           for i = 1:length(input)
              input{i} = varargin{i}.VectorObject; 
           end
           obj.VectorObject.addBoundaryVariables(input);
        end
        
        function advance(obj)
            obj.VectorObject.advance();
        end
        
        function ret = hasNext(obj)
            ret = obj.VectorObject.hasNext();
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
                    args{i} = varargin{i}.VectorObject;
                else
                    error('not supported');
                end
            end
            
            pfs = obj.VectorObject.addRepeatedFactor(factor.VectorObject,bufferSize,args);
            factorStream = FactorGraphStream(pfs);
        end
        
        function retval = addFactorNoCache(obj,firstArg,varargin)
            retval = obj.addFactorWithCacheFlag(false,firstArg,varargin{:});
        end
        
        function factor = addDirectedFactor(obj,factor,variables,directedTo)
            if ~ iscell(factor)
                factor = {factor};
            end
            factor = obj.addFactor(factor{:},variables{:});
            factor.DirectedTo = directedTo;
        end
        
        
        function retval = addFactorVectorized(obj,firstArg,varargin)
            %addFactorVectorized can be used to speed up the creation of
            %large graphs with many factors.
            %
            % As an example, the following code can be used in computer
            % vision algorithms to create factors between all adjacent
            % pixels:
            %
            % a = Discrete(domain,M,N);
            % fg.addFactorVectorized(@myfactor,a(1:(end-1),:),a(2:end,:));
            % fg.addFactorVectorized(@myfactor,a(:,1:end-1),a(:,2:end));
            %
            % addFactorVectorized also supports vectorizing over subsets of
            % the dimensions.  In such cases, the user must pass a cell
            % array containing two elements.  The first is the variable and
            % the second is an array of dimensions to vectorize.
            %
            % The following code will vectorize over the MxN 3x2 chunks so
            % that myweirdfactor gets called with 3x2 bits.
            %
            % x = Bit(M,3,N,2);
            % fg.addFactorVectorized(@myweirdfactor,{x,[1,3]});
            %
            % addFactorVectorized works with Discrete, Reals, and Nested
            % Graphs.
            
            %Get variables
            [firstvars,numLeft] = obj.extractFirstArgs(varargin{:});
            [firstFactor,isVectorIndicesAndWeights] = obj.addFactor(firstArg,firstvars{:});
            if isVectorIndicesAndWeights
                varargin = varargin(2:end);
            end
            retval = firstFactor;
            
            if numLeft
                
                [finalvars,indices] = obj.extractFinalArgs(varargin{:});

                %Check to see if this is a 2 long vector.  If it is, we
                %special case it since Matlab to java calls don't keep row
                %vectors as 1,N matrices.
                if numLeft == 1
                    for i = 1:length(finalvars)
                        finalvars{i} = wrapProxyObject(finalvars{i});
                    end
                    
                    otherFactors = obj.addFactor(firstArg,finalvars{:}).VectorObject;
                else

                    if isa(firstFactor,'FactorGraph')
                        graph = firstFactor.VectorObject;
                        otherFactors = obj.VectorObject.addGraphVectorized(graph,finalvars,indices);
                    else
                        factor = firstFactor.VectorObject;
                        otherFactors = obj.VectorObject.addFactorVectorized(factor,finalvars,indices);
                    end
                end
                retval = wrapProxyObject(otherFactors);
                retval = [firstFactor retval];
                
                dimsizes = obj.extractFactorDimensions(varargin{:});
               
                retval = reshape(retval,dimsizes);
            end
        end
        
        function [retval,isVectorIndicesAndWeights] = addFactor(obj,firstArg,varargin)
            %Examples:
            % fg.addFactor(someFunction,var1,var2);
            [retval, isVectorIndicesAndWeights] = obj.addFactorWithCacheFlag(true,firstArg,varargin{:});
        end
        
        function str = getAdjacencyString(obj)
            str = obj.VectorObject.getAdjacencyString();
        end
        function str = getNodeString(obj)
            str = obj.VectorObject.getNodeString();
        end
        function str = getFullString(obj)
            str = obj.VectorObject.getFullString();
        end
        
        function graphs = get.NestedGraphs(obj)
            VectorObjects = cell(obj.VectorObject.getNestedGraphs());
            
            graphs = cell(numel(VectorObjects),1);
            
            for i = 1:numel(VectorObjects)
                graphs{i} = FactorGraph('VectorObject',VectorObjects{i});
            end
        end
        
        function baumWelch(obj,factorsAndTables,numRestarts,numSteps)
            if ~ iscell(factorsAndTables)
                factorsAndTables = {factorsAndTables};
            end
            ifandt = cell(size(factorsAndTables));
            for i = 1:length(factorsAndTables)
                if isa(factorsAndTables{i},'Factor')
                    ifandt{i} = factorsAndTables{i}.VectorObject;
                elseif isa(factorsAndTables{i},'FactorTable')
                    ifandt{i} = factorsAndTables{i}.ITable;
                else
                    error('Second argument should be an array of FactorTables and/or Factors');
                end
            end
            obj.VectorObject.baumWelch(ifandt,numRestarts,numSteps);
        end
        
        function estimateParameters(obj,factorsAndTables,numRestarts,numSteps,stepScaleFactor)
            if ~ iscell(factorsAndTables)
                factorsAndTables = {factorsAndTables};
            end
            ifandt = cell(size(factorsAndTables));
            for i = 1:length(factorsAndTables)
                if isa(factorsAndTables{i},'Factor')
                    ifandt{i} = factorsAndTables{i}.VectorObject;
                elseif isa(factorsAndTables{i},'FactorTable')
                    ifandt{i} = factorsAndTables{i}.ITable;
                else
                    error('Second argument should be an array of FactorTables and/or Factors');
                end
            end
            obj.VectorObject.estimateParameters(ifandt,numRestarts,numSteps,stepScaleFactor);
        end
        
        
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
            
            tmp = obj.VectorObject.getVariableVector(relativeNestingDepth,forceIncludeBoundaryVariables);
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
            tmp = cell(obj.VectorObject.getNonGraphFactors(relativeNestingDepth));
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
            tmp = obj.VectorObject.getFactors(relativeNestingDepth);
            factors = cell(tmp.size(),1);
            for i = 1:tmp.size()
                factors{i} = wrapProxyObject(tmp.getSlice(i-1));
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
        
        function disp(obj)
            disp(obj.Label);
        end
        
        function ret = getFactorByName(obj, name)
            ret = [];
            tmp = obj.VectorObject.getFactorByName(name);
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
            tmp = obj.VectorObject.getFactorByUUID(uuid);
            if tmp ~= 0
                if tmp.isDiscrete()
                    ret = DiscreteFactor(tmp);
                else
                    ret = Factor(tmp);
                end
            end
        end
        
        function ret = getVariableByName(obj, name)
            ret = wrapProxyObject(obj.VectorObject.getVariableByName(name));
        end
        function ret = getVariableByUUID(obj, uuid)
            ret = wrapProxyObject(obj.VectorObject.getVariableByUUID(uuid));
        end
        
        function ret = getGraphByName(obj, name)
            ret = [];
            tmp = obj.VectorObject.getGraphByName(name);
            if tmp ~= 0
                ret = FactorGraph('nestedGraph', tmp);
            end
        end
        
        function ret = getGraphByUUID(obj, uuid)
            ret = [];
            tmp = obj.VectorObject.getGraphByUUID(uuid);
            if tmp ~= 0
                ret = FactorGraph('nestedGraph', tmp);
            end
        end
        
        function isforest = isForest(obj,relativeNestingDepth)
            if nargin < 2
                relativeNestingDepth = intmax;
            end
            isforest = obj.VectorObject.isForest(relativeNestingDepth);
        end
            
        function istree = isTree(obj,relativeNestingDepth)
            if nargin < 2
                relativeNestingDepth = intmax;
            end
            
            istree = obj.VectorObject.isTree(relativeNestingDepth);
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
            inode = node.VectorObject;
            
            inodes = cell(obj.VectorObject.depthFirstSearch(inode,searchDepth,relativeNestingDepth));
            
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
        
        function solveOneStep(obj)
           obj.genericSolve(@() obj.VectorObject.startSolveOneStep(), ...
                            @() obj.VectorObject.solveOneStep());
        end

        function continueSolve(obj)
           obj.genericSolve(@() obj.VectorObject.startContinueSolve(), ...
                            @() obj.VectorObject.continueSolve());
        end

        function solve(obj)

            obj.genericSolve(@() obj.VectorObject.startSolver(),...
                @() obj.VectorObject.solve());
            
        end
        
        
        % Use the scheduler to create a schedule for this graph
        function setScheduler(obj, scheduler)
            if ischar(scheduler)
                registry = SchedulerRegistry();
                scheduler = registry.get(scheduler);
                scheduler = scheduler();
            end
                        
            obj.VectorObject.setScheduler(scheduler);
        end
        
        function set.Scheduler(obj,scheduler)
            obj.setScheduler(scheduler);
        end
        
        function scheduler = get.Scheduler(obj)
            scheduler = Scheduler(obj);
        end

        
        function set.Schedule(obj,schedule)
            %Make sure schedule is a cell array
            
            %Convert variable vectors to variables?
            for i = 1:length(schedule)
                tmp = schedule{i};
                
                if isa(tmp,'cell')
                    % Could either be an edge or block entry
                    e = tmp(1);
                    if isa(e,'cell')
                        e = e{1};
                    end
                    if isa(e, 'com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater')
                        % Must be a block entry
       
                        scheduleEntry = cell(1,length(tmp));
                        scheduleEntry{1} = e;
                        for j=2:length(tmp)
                            scheduleEntry{j} = tmp{j}.VectorObject;
                        end
                        schedule{i} = scheduleEntry;
                    else
                        % Must be an edge entry
                        if length(tmp) ~= 2
                            error('Expected a list of two elements if trying to specify an edge.');
                        end
                        schedule{i} = {tmp{1}.VectorObject,tmp{2}.VectorObject};
                    end
                else
                    schedule{i} = tmp.VectorObject;
                end
            end
            
            obj.VectorObject.setSchedule(schedule);
            
            %Pass down to FactorGraph
        end
        
        function out = getFactorGraphDiffsByName(obj,input)
            out = obj.VectorObject.getFactorGraphDiffsByName(input.VectorObject);
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
            v = variable.getSingleNode();
            ifactors = cell(size(varargin));
            
            for i = 1:numel(varargin)
                tmp = varargin{i};
                ifactors{i} = tmp.VectorObject;
            end
            
            VectorObject = obj.VectorObject.split(v,ifactors);
            
            newvar = variable.createObject(VectorObject,0);
            equalsFactor = newvar.Factors{1};
        end
        
        function [x,y] = plot(obj,varargin)
            %Plots the graph.
            
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
                            %figure out if this is a boundary variable
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
            pnode = node.VectorObject;

            ancestor = obj.VectorObject.isAncestorOf(pnode);
        end
        
        
        
        
        function removeFactor(obj,factor)
            obj.VectorObject.removeFactor(factor.VectorObject);
        end
        
        function setSolver(obj,solver,varargin)
            if ischar(solver)
                registry = getSolverRegistry();
                solver = registry.get(solver);
                solver = solver(varargin{:});
            end
            
            obj.VectorObject.setSolver(solver);
        end
        
        % Define a group of variables to be used by other functions that
        % operate directly on pre-defined variable groups (referred to by
        % their variableGroupID). These variable groups are for variables
        % that are not all part of a single variable vector or matrix.
        % Each input argument can be a varilable or variable matrix.
        function variableGroupID = defineVariableGroup(obj, varargin)
            varargin = [varargin{:}];
            variables = {};
            if isa(varargin,'VariableBase')
                variables = varargin.VectorObject;
            elseif (iscell(varargin))
                for i = 1:length(varargin)
                    if isa(varargin{i},'VariableBase')
                        variables = [variables varargin{i}.VectorObject];
                    end
                end
            end
            variableGroupID = obj.VectorObject.addVariableBlock(variables);
        end

    end
    
    methods (Access = private)
        
        
        function genericSolve(obj,threadedFunction,unthreadedFunction)
                        %Calls solve on the underlying solver object.
            
            if nargin < 3
                unthreadedFunction = @() error('not supported');
            end
            
            % See if the solver has a MATLAB-specific wrapper for its
            % current parameter settings, and if so invoke that instead.
            solveFunc = [];
            try
                matlabSolve = char(obj.VectorObject.getMatlabSolveWrapper());
                if (~isempty(matlabSolve))
                    solveFunc = str2func(matlabSolve);
                end
            catch err
            end
            if (~isempty(solveFunc))
                solveFunc(obj);
                return;
            end
            
            try
                % Rather than directly calling the solver, try to start the
                % solver as a separate thread and then polls the solver to
                % wait for it to either complete or be interrupted by
                % CTRL-C.  To account for the possibility of being
                % interrupted, the onCleanup function terminateSolver makes
                % sure the solver thread is terminated.
                
                if (~obj.VectorObject.isSolverRunning())
                    c = onCleanup(@() obj.terminateSolver());
                    threadedFunction();
                    while obj.VectorObject.isSolverRunning();
                        pause(0.01);
                    end
                else
                    error('Attempt to run solver while it is already running');
                end
            catch Err
                % For backward compatability with solvers that don't
                % support running in a separate thread
                if (strcmp(Err.identifier,'MATLAB:noSuchMethodOrField'))
                    unthreadedFunction();
                else
                    throw(Err);
                end
            end
        end
        
        function [retval, isVectorIndicesAndWeights] = addFactorWithCacheFlag(obj,doCache,firstArg,varargin)
            isVectorIndicesAndWeights = 0;
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
                        error('When adding a table with addFactor, user should specify both VectorIndices and values');
                    end
                    
                    if isa(varargin{1},'double')
                        isVectorIndicesAndWeights = 1;
                        if numel(varargin) < 2
                            error('need at least one variable');
                        end
                        retval = obj.addTableFromVectorIndicesAndValues(firstArg,varargin{1},{varargin{2:end}});
                    else
                        retval = obj.addTableFromValues(firstArg,varargin);
                        
                    end
                    
                elseif ischar(firstArg)
                    % First argument is a string
                    % Either a custom factor or a Java FactorFunction
                    % Custom factor takes precidence
                    
                    customFuncExists = obj.VectorObject.customFactorExists(firstArg);
            
                    if customFuncExists
                        % It is a custom factor
                        
                        %TODO: this should be shared with non custom func code?
                        for i = 1:length(varargin)
                            if isa(varargin{i},'VariableBase')
                                varargin{i} = varargin{i}.VectorObject;
                            end
                        end
                        retval = Factor(obj.VectorObject.createCustomFactor(firstArg,varargin),0);
                    else
                        % Must be a Java FactorFunction
                        
                        % TODO: avoid creating the same factor-function
                        % over and over if it's already created
                        registry = FactorFunctionRegistry();
                        factorFunction = registry.get(firstArg);
                        factorFunction = factorFunction();  % Assume no constructor arguments
                        retval = obj.addJavaFactorFunction(factorFunction,varargin{:});
                    end
                    
                elseif (isa(firstArg,'FactorFunction'))
                    retval = obj.addJavaFactorFunction(firstArg.get(),varargin{:});
                    
                     % TODO: replace with a function call isFactorFunction
                elseif (isa(firstArg, 'com.analog.lyric.dimple.factorfunctions.core.FactorFunction'))
                    retval = obj.addJavaFactorFunction(firstArg,varargin{:});
                    
                elseif iscell(firstArg)
                    factorFunction = FactorFunction(firstArg{:});
                    retval = obj.addJavaFactorFunction(factorFunction.get(),varargin{:});

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
                nodes{i} = nodes{i}.VectorObject;
            end
            
            
            A = double(obj.VectorObject.getAdjacencyMatrix(nodes));
            
        end
        
        
        function flatCell = flattenNodes(obj,nodes)
            num = 0;
            for i = 1:length(nodes)
                if ~isa(nodes{i},'Factor') && ~isa(nodes{i},'FactorGraph');
                    num = num+nodes{i}.VectorObject.size();
                else
                    num = num+1;
                end
                
            end
            
            flatCell = cell(num,1);
            index = 1;
            for i = 1:length(nodes)
                if ~isa(nodes{i},'Factor') && ~isa(nodes{i},'FactorGraph');
                    for j = 1:nodes{i}.VectorObject.size()
                        flatCell{index} = wrapProxyObject(nodes{i}.VectorObject.getSlice(j-1));
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
                factors{i} = varargin{i}.VectorObject;
            end
            
            factor = wrapProxyObject(obj.VectorObject.joinFactors(factors));
        end
        
        function var = joinVariables(obj,varargin)
            
            %convert oldvars to list of variables
            vars = cell(length(varargin),1);
            for i = 1:length(varargin)
                if varargin{i}.VectorObject.size() ~= 1
                    error('vectors not supported');
                end
                tmp = varargin{i}.VectorObject.getSlice(0);
                vars{i} = tmp;
            end
            
            %call join on VectorObject
            tmpVar = obj.VectorObject.joinVariables(vars);
            
            %TODO: figure out domain differently
            if tmpVar.isDiscrete()
                var = Discrete(cell(tmpVar.getSlice(0).getDomain().getElements()),'existing',tmpVar,0);
            else
                error('not yet supported');
                %var = Real(tmpVar.getDomain(),'existing',tmpVar,1);
            end
            %create new variable
        end
        
        function retval = addGraph(parentGraph,childGraph,varargin)
            
            VectorObject = parentGraph.getVarVector(varargin{:});
            newGraph = parentGraph.VectorObject.addGraph(childGraph.VectorObject,VectorObject);
            if isempty(newGraph)
                retval = [];
            else
                retval = FactorGraph('nestedGraph',newGraph);
            end
        end
        
        function retval = addTable(obj,table,varargin)
            
            VectorObject = obj.getVarVector(varargin{:});
            
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
            
            
            func = obj.VectorObject.createFactor(table.ITable,VectorObject);
            retval = wrapProxyObject(func);
        end
        
        
        function retval = addFunctionHandle(obj,doCache,funcHandle,varargin)
            
            %Modify this function to look at the class of funcHandle
            %Do something different based on function_handle, double, or
            %FactorGraph
            retval = [];
            funcName = func2str(funcHandle);
            customFuncExists = obj.VectorObject.customFactorExists(funcName);
            %customFuncExists = obj.VectorObject.getSolver().customFactorExists(obj.VectorObject,funcName);
            
            if customFuncExists
                
                
                %TODO: this should be shared with non custom func code?
                
                for i = 1:length(varargin)
                    if isa(varargin{i},'VariableBase')
                        varargin{i} = varargin{i}.VectorObject;
                    end
                end
                
                retval = Factor(obj.VectorObject.createCustomFactor(funcName,varargin),0);
                
            else
                
                %make sure this is a discrete factor
                VectorObject = [];
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
                        %j indexes the entry into a VectorObject.  Because this
                        %is a constant, we only have one variable
                        domains{i} = {DiscreteDomain({varargin{i}})};
                        
                    else
                        %if we get here, this is in fact a variable
                        
                        %allocate enough domains for each element in the
                        %VectorObject
                        domains{i} = cell(size(varargin{i}.VectorIndices));
                        
                        
                        ind = varargin{i}.VectorIndices;
                        
                        %for each entry in the VectorIndices
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
                        if isempty(VectorObject)
                            VectorObject = varargin{i}.VectorObject;
                        else
                            VectorIndices = [0:VectorObject.size()-1 0:varargin{i}.VectorObject.size()-1];
                            VectorObjectVectorIndices = [zeros(1,VectorObject.size()) ones(1,varargin{i}.VectorObject.size())];
                            VectorObject = VectorObject.concat({VectorObject varargin{i}.VectorObject},VectorObjectVectorIndices,VectorIndices);
                        end
                    end
                    
                end
                
                if doCache
                    table = obj.TableFactory.getTable(funcHandle,domains,constants);
                else
                    table = FunctionEntry.createFactorTable(func2str(funcHandle),...
                        domains,constants,funcHandle);
                end
                
                retval = DiscreteFactor(obj.VectorObject.createFactor(table{3},VectorObject),0);
            end
        end
        
        
        function retval = addJavaFactorFunction(obj,factorFunction,varargin)
            
            %TODO: this should be shared with code above?
            %vars = cell(length(varargin));
            for i = 1:length(varargin)
                if isa(varargin{i},'VariableBase')
                    varargin{i} = varargin{i}.VectorObject;
                end
            end
            
            
            retval = Factor(obj.VectorObject.createFactor(factorFunction,varargin));
        end
        
        
        function [VectorObject] = getVarVector(obj,varargin)
            
            VectorObject = [];
            
            for i = 1:length(varargin)
                if i == 1
                    VectorObject = varargin{i}.VectorObject;
                else
                    VectorObject = VectorObject.concat({VectorObject varargin{i}.VectorObject});
                end
            end
            
        end
        
        
        function retval = addTableFromVectorIndicesAndValues(obj,VectorIndices,values,variables)
            domains = obj.getDomainsFromVariableList(variables);
            table = FactorTable(VectorIndices,values,domains{:});
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
            if obj.VectorObject.isSolverRunning();
                obj.VectorObject.interruptSolver();
            end
        end
        
        % The following methods are used for addFactorVectorized
        
        %This method reorders the variable dimensions for addFactorVectorized
        %dimensions specifies which dimensions we want to loop over in
        %order to create multiple factors
        %This method moves those dimensions to the right and all other
        %dimensions to the left so that Java can simply loop over chunks
        %of variables.
        function var = reorderArg(obj,arg,dimensions)
            %first figure out how to permute
            numdims = length(size(arg));
            alldims = 1:numdims;
            
            %Get the dimensions we will not vectorize.  These get passed in
            %bulk to the factor function
            unvecdims = setxor(alldims,dimensions);
            
            %Determine the permutation order.
            permuteorder = zeros(numdims,1);
            index = 1;
            
            %Find the dimensions that will be vectorized
            vecsize = 1;
            for i = 1:length(alldims)
                if ismember(alldims(i),dimensions)
                    permuteorder(index) = alldims(i);
                    index = index + 1;
                    vecsize = vecsize * size(arg,i);
                end
            end
            
            %Then find the dimensions that will not be vectorized
            unvecsize = 1;
            for i = 1:length(alldims)
                if ismember(alldims(i),unvecdims)
                    permuteorder(index) = alldims(i);
                    index = index + 1;
                    unvecsize = unvecsize * size(arg,i);
                end
            end            
            
            %Permute the variable
            var = arg.createObject(arg.VectorObject,permute(arg.VectorIndices,permuteorder));            
            var = reshape(var,vecsize,unvecsize);
            
        end
        
        %used by addFactorVectorized.  For a single variable, re-order the
        %dimensions of the variable, remove the first variable if there's
        %more than one (since we've already called addFactor for that, and
        %record the number of factors and numvarsperfactor.
        %
        %arg - The VectorObject we will pass to the java addFactorVectorized
        %numvarsperfactor - The number of variables in a row for a single
        %factor.
        %numfactors - The number of sets of variables.  This should be
        %either the number of factors that will be created or 1 if the
        %variable set is shared across factors.
        function [arg,indices] = extractFinalArg(obj,input)
            if isa(input,'VariableBase')
                if prod(size(input)) > 1
                    input = input(2:end);
                end
                arg = input.VectorObject;
                indices = input.VectorIndices(:);
            elseif iscell(input) && length(input) == 2 && isa(input{1},'VariableBase')
                %TODO: Figure
                newarg = obj.reorderArg(input{1},input{2});
                if size(newarg,1) > 1
                    newarg = newarg(2:end,:);
                end
                arg = newarg.VectorObject;
                indices = newarg.VectorIndices;
            else
                arg = [];
                indices = [];
            end
        end
        
        %We extract the first variable of every variable vector in order to
        %make the first addFactor call.
        function [arg,numLeft] = extractFirstArg(obj,input)
            numLeft = 0;
            if isa(input,'VariableBase')
                arg = input(1);
                if length(input) > 1
                    numLeft = prod(size(input))-1;
                end
            elseif iscell(input) && length(input) == 2 && isa(input{1},'VariableBase')
                newarg = obj.reorderArg(input{1},input{2});
                arg = newarg(1,:);
                if size(newarg,1) > 1
                    numLeft = size(newarg,1)-1;
                end
            else
                arg = input;
            end
        end
        
        %Extract all the first vars to call addFactor
        function [firstvars,anythingLeft] = extractFirstArgs(obj,varargin)
            firstvars = cell(size(varargin));
            anythingLeft = 0;
            for i = 1:length(firstvars)
                [firstvars{i},tmp] = obj.extractFirstArg(varargin{i});
                if tmp
                    anythingLeft = tmp;
                end
            end
        end
        
        %Extract the remaining variables (after the extarctFirstArgs) to
        %call addFactorVectorized
        function [finalvars,indices] = extractFinalArgs(obj,varargin)
            finalvars = {};
            indices = {};
            
            maxIndices = 0;
            
            for i = 1:length(varargin)
                [var,ind] = obj.extractFinalArg(varargin{i});
                if ~isempty(ind)
                    finalvars{length(finalvars)+1} = var;
                    indices{length(indices)+1} = ind;
                    if size(ind,1) > maxIndices
                        maxIndices = size(ind,1);
                    end
                end
            end
            
            for i = 1:length(finalvars)
               tmp = size(indices{i},1);
               if tmp == 1
                   indices{i} = repmat(indices{i},maxIndices,1);
               elseif tmp ~= maxIndices
                   error('mismatch of matrix dimensions');
               end
            end
        end
        
        function dimsizes = extractFactorDimensions(obj,varargin)
            %This method is used to reshape the Factors returned by
            %add Factor.  It uses the list of variables passed in to
            %determine the dimensions of the factors.  In addition, it
            %verifies that the variable argument list is consistent.
            
            dimsizes = 1;
            for i = 1:length(varargin)
                tmp = varargin{i};
                if isa(tmp,'VariableBase')
                    tmpdimsizes = size(tmp);
                elseif iscell(tmp) && ~isempty(tmp) && isa(tmp{1},'VariableBase')
                    dims = tmp{2};
                    
                    if isempty(dims)
                        tmpdimsizes = [1 1];
                    else 
                        tmpdimsizes = ones(1,length(size(tmp{1})));
                        for j = 1:length(dims)
                            tmpdimsizes(dims(j)) = size(tmp{1},dims(j));
                        end
                    end
                else
                    tmpdimsizes = [1 1];
                end
                
                %remove all ones in the middle.
                tmp = tmpdimsizes(2:end);
                tmp = tmp(tmp > 1);
                if isempty(tmp)
                    tmp = 1;
                end
                tmpdimsizes = [tmpdimsizes(1) tmp];                
                
                %remove any trailing ones
                
                if ~isequal(tmpdimsizes,[1 1])
                    if ~isequal(dimsizes,1) && ~isequal(dimsizes,tmpdimsizes)
                        error('variable sizes dont match');
                    end
                    dimsizes = tmpdimsizes;
                end
            end
            if length(dimsizes) == 1
                dimsizes = [dimsizes 1];
            end
        end
        
    end
    
    methods(Access=protected)
        function setSolverInternal(obj,solver)
            obj.setSolver(solver);
        end
        function retval = createObject(obj,vectorObject,VectorIndices)
            retval = FactorGraph('VectorObject',vectorObject,VectorIndices);
        end
        
        function verifyCanConcatenate(obj,otherObjects)
            
        end
        
    end
end
