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

function testDomains()

debugPrint = false;
repeatable = true;

dtrace(debugPrint, '++testDomains');

test1(debugPrint, repeatable);
dtrace(debugPrint, '--testDomains');

end


function test1(debugPrint, repeatable)

b = Bit;
ba = Bit(2,3);
bq = Bit(2);
assert(isa(b.Domain, 'DiscreteDomain'));
assert(isa(bq.Domain, 'DiscreteDomain'));
assert(isa(ba.Domain, 'DiscreteDomain'));
assertEqual(cell2mat(b.Domain.Elements), [0 1]);
assertEqual(cell2mat(ba.Domain.Elements), [0 1]);
assertEqual(cell2mat(bq.Domain.Elements), [0 1]);
assertEqual(size(b), [1 1]);
assertEqual(size(ba), [2 3]);
assertEqual(size(bq), [2 2]);

dRange = 1:10;
dDomain = DiscreteDomain(dRange);
d = Discrete(dRange);
dd = Discrete(dDomain);
da = Discrete(dRange,2,3);
dda = Discrete(dDomain,2,3);
dq = Discrete(dRange,2);
ddq = Discrete(dDomain,2);
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
assertEqual(size(d), [1 1]);
assertEqual(size(dd), [1 1]);
assertEqual(size(da), [2 3]);
assertEqual(size(dda), [2 3]);
assertEqual(size(dq), [2 2]);
assertEqual(size(ddq), [2 2]);

L = -7;
U = 12.2;
rInf = [-Inf Inf];
rInfDomain = RealDomain(-Inf, Inf);
rRange = [L U];
rDomain = RealDomain(L, U);
r = Real();
ri = Real(rInf);
rid = Real(rInfDomain);
rr = Real(rRange);
rrd = Real(rDomain);
ra = Real(2,3);
ria = Real(rInf,2,3);
rida = Real(rInfDomain,2,3);
rra = Real(rRange,2,3);
rrda = Real(rDomain,2,3);
rq = Real(2);
riq = Real(rInf,2);
ridq = Real(rInfDomain,2);
rrq = Real(rRange,2);
rrdq = Real(rDomain,2);
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
assertEqual(size(r), [1 1]);
assertEqual(size(ri), [1 1]);
assertEqual(size(rid), [1 1]);
assertEqual(size(rr), [1 1]);
assertEqual(size(rrd), [1 1]);
assertEqual(size(ra), [2 3]);
assertEqual(size(ria), [2 3]);
assertEqual(size(rida), [2 3]);
assertEqual(size(rra), [2 3]);
assertEqual(size(rrda), [2 3]);
assertEqual(size(riq), [2 2]);
assertEqual(size(ridq), [2 2]);
assertEqual(size(rrq), [2 2]);
assertEqual(size(rrdq), [2 2]);

L2 = 7.2;
U2 = 27;
rDomain2 = RealDomain(L2, U2);
cDomain = ComplexDomain(rDomain, rDomain2);
ccDomain = ComplexDomain(rDomain);
c = Complex();
cd = Complex(cDomain);
ca = Complex(2,3);
cda = Complex(cDomain,2,3);
cq = Complex(2);
cdq = Complex(cDomain,2);
cc = Complex(ccDomain);
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
assertEqual(size(c), [1 1]);
assertEqual(size(cd), [1 1]);
assertEqual(size(ca), [2 3]);
assertEqual(size(cda), [2 3]);
assertEqual(size(cq), [2 2]);
assertEqual(size(cdq), [2 2]);
assertEqual(size(cc), [1 1]);
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
j = RealJoint(3);
jd = RealJoint(jDomain);
ja = RealJoint(3,4,5);
jda = RealJoint(jDomain,4,5);
jq = RealJoint(3,4);
jdq = RealJoint(jDomain,4);
jj = RealJoint(jjDomain);
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
assertEqual(size(j), [1 1]);
assertEqual(size(jd), [1 1]);
assertEqual(size(ja), [4 5]);
assertEqual(size(jda), [4 5]);
assertEqual(size(jq), [4 4]);
assertEqual(size(jdq), [4 4]);
assertEqual(size(jj), [1 1]);
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
		
