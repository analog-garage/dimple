%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%   Copyright 2014 Analog Devices, Inc.
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

function testStreamDomains()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testStreamDomains');

test1(debugPrint, repeatable);
dtrace(debugPrint, '--testStreamDomains');

end


function test1(debugPrint, repeatable)

b = BitStream;
ba = BitStream(2,3);
bq = BitStream(2);
assert(isa(b.Domain, 'DiscreteDomain'));
assert(isa(bq.Domain, 'DiscreteDomain'));
assert(isa(ba.Domain, 'DiscreteDomain'));
assertEqual(cell2mat(b.Domain.Elements), [0 1]);
assertEqual(cell2mat(ba.Domain.Elements), [0 1]);
assertEqual(cell2mat(bq.Domain.Elements), [0 1]);
assertEqual(b.Dimensions, [1 1]);
assertEqual(ba.Dimensions, [2 3]);
assertEqual(bq.Dimensions, [2 2]);

dRange = 1:10;
dDomain = DiscreteDomain(dRange);
d = DiscreteStream(dRange);
dd = DiscreteStream(dDomain);
da = DiscreteStream(dRange,2,3);
dda = DiscreteStream(dDomain,2,3);
dq = DiscreteStream(dRange,2);
ddq = DiscreteStream(dDomain,2);
assert(isa(d.Domain, 'DiscreteDomain'));
assert(isa(dd.Domain, 'DiscreteDomain'));
assert(isa(da.Domain, 'DiscreteDomain'));
assert(isa(dda.Domain, 'DiscreteDomain'));
assert(isa(dq.Domain, 'DiscreteDomain'));
assert(isa(ddq.Domain, 'DiscreteDomain'));
assertEqual(cell2mat(d.Domain.Elements), dRange);
assertEqual(cell2mat(dd.Domain.Elements), dRange);
assertEqual(cell2mat(da.Domain.Elements), dRange);
assertEqual(cell2mat(dda.Domain.Elements), dRange);
assertEqual(cell2mat(dq.Domain.Elements), dRange);
assertEqual(cell2mat(ddq.Domain.Elements), dRange);
assertEqual(d.Dimensions, [1 1]);
assertEqual(dd.Dimensions, [1 1]);
assertEqual(da.Dimensions, [2 3]);
assertEqual(dda.Dimensions, [2 3]);
assertEqual(dq.Dimensions, [2 2]);
assertEqual(ddq.Dimensions, [2 2]);

L = -7;
U = 12.2;
rInf = [-Inf Inf];
rInfDomain = RealDomain(-Inf, Inf);
rRange = [L U];
rDomain = RealDomain(L, U);
r = RealStream();
ri = RealStream(rInf);
rid = RealStream(rInfDomain);
rr = RealStream(rRange);
rrd = RealStream(rDomain);
ra = RealStream(2,3);
ria = RealStream(rInf,2,3);
rida = RealStream(rInfDomain,2,3);
rra = RealStream(rRange,2,3);
rrda = RealStream(rDomain,2,3);
rq = RealStream(2);
riq = RealStream(rInf,2);
ridq = RealStream(rInfDomain,2);
rrq = RealStream(rRange,2);
rrdq = RealStream(rDomain,2);
assert(isa(r.Domain, 'RealDomain'));
assert(isa(ri.Domain, 'RealDomain'));
assert(isa(rid.Domain, 'RealDomain'));
assert(isa(rr.Domain, 'RealDomain'));
assert(isa(rrd.Domain, 'RealDomain'));
assert(isa(ra.Domain, 'RealDomain'));
assert(isa(ria.Domain, 'RealDomain'));
assert(isa(rida.Domain, 'RealDomain'));
assert(isa(rra.Domain, 'RealDomain'));
assert(isa(rrda.Domain, 'RealDomain'));
assert(isa(rq.Domain, 'RealDomain'));
assert(isa(riq.Domain, 'RealDomain'));
assert(isa(ridq.Domain, 'RealDomain'));
assert(isa(rrq.Domain, 'RealDomain'));
assert(isa(rrdq.Domain, 'RealDomain'));
assertEqual(r.Domain.LB, -Inf);
assertEqual(r.Domain.UB, Inf);
assertEqual(ri.Domain.LB, -Inf);
assertEqual(ri.Domain.UB, Inf);
assertEqual(rid.Domain.LB, -Inf);
assertEqual(rid.Domain.UB, Inf);
assertEqual(rr.Domain.LB, L);
assertEqual(rr.Domain.UB, U);
assertEqual(rrd.Domain.LB, L);
assertEqual(rrd.Domain.UB, U);
assertEqual(ra.Domain.LB, -Inf);
assertEqual(ra.Domain.UB, Inf);
assertEqual(ria.Domain.LB, -Inf);
assertEqual(ria.Domain.UB, Inf);
assertEqual(rida.Domain.LB, -Inf);
assertEqual(rida.Domain.UB, Inf);
assertEqual(rra.Domain.LB, L);
assertEqual(rra.Domain.UB, U);
assertEqual(rrda.Domain.LB, L);
assertEqual(rrda.Domain.UB, U);
assertEqual(rq.Domain.LB, -Inf);
assertEqual(rq.Domain.UB, Inf);
assertEqual(riq.Domain.LB, -Inf);
assertEqual(riq.Domain.UB, Inf);
assertEqual(ridq.Domain.LB, -Inf);
assertEqual(ridq.Domain.UB, Inf);
assertEqual(rrq.Domain.LB, L);
assertEqual(rrq.Domain.UB, U);
assertEqual(rrdq.Domain.LB, L);
assertEqual(rrdq.Domain.UB, U);
assertEqual(r.Dimensions, [1 1]);
assertEqual(ri.Dimensions, [1 1]);
assertEqual(rid.Dimensions, [1 1]);
assertEqual(rr.Dimensions, [1 1]);
assertEqual(rrd.Dimensions, [1 1]);
assertEqual(ra.Dimensions, [2 3]);
assertEqual(ria.Dimensions, [2 3]);
assertEqual(rida.Dimensions, [2 3]);
assertEqual(rra.Dimensions, [2 3]);
assertEqual(rrda.Dimensions, [2 3]);
assertEqual(riq.Dimensions, [2 2]);
assertEqual(ridq.Dimensions, [2 2]);
assertEqual(rrq.Dimensions, [2 2]);
assertEqual(rrdq.Dimensions, [2 2]);

L2 = 7.2;
U2 = 27;
rDomain2 = RealDomain(L2, U2);
cDomain = ComplexDomain(rDomain, rDomain2);
ccDomain = ComplexDomain(rDomain);
c = ComplexStream();
cd = ComplexStream(cDomain);
ca = ComplexStream(2,3);
cda = ComplexStream(cDomain,2,3);
cq = ComplexStream(2);
cdq = ComplexStream(cDomain,2);
cc = ComplexStream(ccDomain);
assert(isa(c.Domain, 'ComplexDomain'));
assert(isa(cd.Domain, 'ComplexDomain'));
assert(isa(ca.Domain, 'ComplexDomain'));
assert(isa(cda.Domain, 'ComplexDomain'));
assert(isa(cq.Domain, 'ComplexDomain'));
assert(isa(cdq.Domain, 'ComplexDomain'));
assert(isa(cc.Domain, 'ComplexDomain'));
assertEqual(c.Domain.NumElements, 2);
assertEqual(cd.Domain.NumElements, 2);
assertEqual(ca.Domain.NumElements, 2);
assertEqual(cda.Domain.NumElements, 2);
assertEqual(cq.Domain.NumElements, 2);
assertEqual(cdq.Domain.NumElements, 2);
assertEqual(cc.Domain.NumElements, 2);
assertEqual(c.Dimensions, [1 1]);
assertEqual(cd.Dimensions, [1 1]);
assertEqual(ca.Dimensions, [2 3]);
assertEqual(cda.Dimensions, [2 3]);
assertEqual(cq.Dimensions, [2 2]);
assertEqual(cdq.Dimensions, [2 2]);
assertEqual(cc.Dimensions, [1 1]);
assertEqual(c.Domain.RealDomains{1}.LB, -Inf);
assertEqual(c.Domain.RealDomains{1}.UB, Inf);
assertEqual(c.Domain.RealDomains{2}.LB, -Inf);
assertEqual(c.Domain.RealDomains{2}.UB, Inf);
assertEqual(cd.Domain.RealDomains{1}.LB, L);
assertEqual(cd.Domain.RealDomains{1}.UB, U);
assertEqual(cd.Domain.RealDomains{2}.LB, L2);
assertEqual(cd.Domain.RealDomains{2}.UB, U2);
assertEqual(ca.Domain.RealDomains{1}.LB, -Inf);
assertEqual(ca.Domain.RealDomains{1}.UB, Inf);
assertEqual(ca.Domain.RealDomains{2}.LB, -Inf);
assertEqual(ca.Domain.RealDomains{2}.UB, Inf);
assertEqual(cda.Domain.RealDomains{1}.LB, L);
assertEqual(cda.Domain.RealDomains{1}.UB, U);
assertEqual(cda.Domain.RealDomains{2}.LB, L2);
assertEqual(cda.Domain.RealDomains{2}.UB, U2);
assertEqual(cq.Domain.RealDomains{1}.LB, -Inf);
assertEqual(cq.Domain.RealDomains{1}.UB, Inf);
assertEqual(cq.Domain.RealDomains{2}.LB, -Inf);
assertEqual(cq.Domain.RealDomains{2}.UB, Inf);
assertEqual(cdq.Domain.RealDomains{1}.LB, L);
assertEqual(cdq.Domain.RealDomains{1}.UB, U);
assertEqual(cdq.Domain.RealDomains{2}.LB, L2);
assertEqual(cdq.Domain.RealDomains{2}.UB, U2);
assertEqual(cc.Domain.RealDomains{1}.LB, L);
assertEqual(cc.Domain.RealDomains{1}.UB, U);
assertEqual(cc.Domain.RealDomains{2}.LB, L);
assertEqual(cc.Domain.RealDomains{2}.UB, U);

L3 = -Inf;
U3 = 0;
rDomain3 = RealDomain(L3, U3);
jDomain = RealJointDomain(rDomain, rDomain2, rDomain3);
jjDomain = RealJointDomain(3, rDomain);
j = RealJointStream(3);
jd = RealJointStream(jDomain);
ja = RealJointStream(3,4,5);
jda = RealJointStream(jDomain,4,5);
jq = RealJointStream(3,4);
jdq = RealJointStream(jDomain,4);
jj = RealJointStream(jjDomain);
assert(isa(j.Domain, 'RealJointDomain'));
assert(isa(jd.Domain, 'RealJointDomain'));
assert(isa(ja.Domain, 'RealJointDomain'));
assert(isa(jda.Domain, 'RealJointDomain'));
assert(isa(jq.Domain, 'RealJointDomain'));
assert(isa(jdq.Domain, 'RealJointDomain'));
assert(isa(jj.Domain, 'RealJointDomain'));
assertEqual(j.Domain.NumElements, 3);
assertEqual(jd.Domain.NumElements, 3);
assertEqual(ja.Domain.NumElements, 3);
assertEqual(jda.Domain.NumElements, 3);
assertEqual(jq.Domain.NumElements, 3);
assertEqual(jdq.Domain.NumElements, 3);
assertEqual(jj.Domain.NumElements, 3);
assertEqual(j.Dimensions, [1 1]);
assertEqual(jd.Dimensions, [1 1]);
assertEqual(ja.Dimensions, [4 5]);
assertEqual(jda.Dimensions, [4 5]);
assertEqual(jq.Dimensions, [4 4]);
assertEqual(jdq.Dimensions, [4 4]);
assertEqual(jj.Dimensions, [1 1]);
assertEqual(j.Domain.RealDomains{1}.LB, -Inf);
assertEqual(j.Domain.RealDomains{1}.UB, Inf);
assertEqual(j.Domain.RealDomains{2}.LB, -Inf);
assertEqual(j.Domain.RealDomains{2}.UB, Inf);
assertEqual(j.Domain.RealDomains{3}.LB, -Inf);
assertEqual(j.Domain.RealDomains{3}.UB, Inf);
assertEqual(jd.Domain.RealDomains{1}.LB, L);
assertEqual(jd.Domain.RealDomains{1}.UB, U);
assertEqual(jd.Domain.RealDomains{2}.LB, L2);
assertEqual(jd.Domain.RealDomains{2}.UB, U2);
assertEqual(jd.Domain.RealDomains{3}.LB, L3);
assertEqual(jd.Domain.RealDomains{3}.UB, U3);
assertEqual(ja.Domain.RealDomains{1}.LB, -Inf);
assertEqual(ja.Domain.RealDomains{1}.UB, Inf);
assertEqual(ja.Domain.RealDomains{2}.LB, -Inf);
assertEqual(ja.Domain.RealDomains{2}.UB, Inf);
assertEqual(ja.Domain.RealDomains{3}.LB, -Inf);
assertEqual(ja.Domain.RealDomains{3}.UB, Inf);
assertEqual(jda.Domain.RealDomains{1}.LB, L);
assertEqual(jda.Domain.RealDomains{1}.UB, U);
assertEqual(jda.Domain.RealDomains{2}.LB, L2);
assertEqual(jda.Domain.RealDomains{2}.UB, U2);
assertEqual(jda.Domain.RealDomains{3}.LB, L3);
assertEqual(jda.Domain.RealDomains{3}.UB, U3);
assertEqual(jq.Domain.RealDomains{1}.LB, -Inf);
assertEqual(jq.Domain.RealDomains{1}.UB, Inf);
assertEqual(jq.Domain.RealDomains{2}.LB, -Inf);
assertEqual(jq.Domain.RealDomains{2}.UB, Inf);
assertEqual(jq.Domain.RealDomains{3}.LB, -Inf);
assertEqual(jq.Domain.RealDomains{3}.UB, Inf);
assertEqual(jdq.Domain.RealDomains{1}.LB, L);
assertEqual(jdq.Domain.RealDomains{1}.UB, U);
assertEqual(jdq.Domain.RealDomains{2}.LB, L2);
assertEqual(jdq.Domain.RealDomains{2}.UB, U2);
assertEqual(jdq.Domain.RealDomains{3}.LB, L3);
assertEqual(jdq.Domain.RealDomains{3}.UB, U3);
assertEqual(jj.Domain.RealDomains{1}.LB, L);
assertEqual(jj.Domain.RealDomains{1}.UB, U);
assertEqual(jj.Domain.RealDomains{2}.LB, L);
assertEqual(jj.Domain.RealDomains{2}.UB, U);
assertEqual(jj.Domain.RealDomains{3}.LB, L);
assertEqual(jj.Domain.RealDomains{3}.UB, U);

end			
		
