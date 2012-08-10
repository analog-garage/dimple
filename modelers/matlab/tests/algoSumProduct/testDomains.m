%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright (c) 2010, Lyric Semiconductor, Inc.
% All rights reserved.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function test_suite = testDomains()
%disp('++testDomains');
initTestSuite;

%function setup
%    disp('testDomains.setup');

%function teardown
%    disp('testDomains.teardown');


function testSetGetSimpleDomain
    %disp('++testSetGetSimpleDomain');
    b = Bit;
    assertEqual(b.Domain.Elements,{0,1});
    v = Variable([2 1 0]);
    assertEqual(v.Domain.Elements,{2, 1, 0});
    %disp('--testSetGetSimpleDomain');
    
function testComplexDomain
    %disp('++testComplexDomain');
    domain = {[1+sqrt(-1) 0+2*sqrt(-1)],[2 1+sqrt(-5)]};
    v = Variable(domain);
    assertEqual(v.Domain.Elements,domain);
    %disp('--testComplexDomain');
    
    
function testDomainWithFunc
    i = sqrt(-1);
    domain = {[1+i 0+2*i],[2 1+-5*i],[2 2]};
    v = Variable(domain);
    assertEqual(v.Domain.Elements,domain);
    g = FactorGraph;
    g.addFactor(@domainDelta,v);
    g.Solver.setNumIterations(2);
    g.solve();
    assertElementsAlmostEqual(v.Belief,[.5 .5 0]);
    
    
function testMatrixDomainDef    
    dcell = {[1 3]',[2 4]'};
    v = Variable(dcell);
    assertEqual(v.Domain.Elements,dcell);
    
function testNdMatrixDomainDef
    d1 = [1 2; 3 4];
    d2 = [5 6; 7 8];
    celld = {d1,d2};
    v = Variable(celld);
    assertEqual(v.Domain.Elements,celld);
    
    
function testColDomain    
    v = Variable({[2 1 0]'});
    d1 = v.Domain.Elements;
    d2 = {[2 1 0]'};
    assertEqual(d1,d2);
    
%disp('--testDomains')
