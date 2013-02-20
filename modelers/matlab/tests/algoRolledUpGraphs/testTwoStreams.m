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

function testTwoStreams()

    in = Bit();
    out = Bit();
    ng = FactorGraph(in,out);
    ng.addFactor(@xorDelta,in,out);

    fg = FactorGraph();
    b = BitStream();

    fg.addFactor(ng,b,b.getSlice(2));

    c = BitStream();

    fg.addFactor(ng,c,c.getSlice(2));

    bsrc = DoubleArrayDataSource();
    csrc = DoubleArrayDataSource();


    bsrc.add(repmat([.8 .2]',1,5));
    csrc.add(repmat([.6 .4]',1,5));

    b.DataSource = bsrc;
    c.DataSource = csrc;

    fg.initialize();

    fgb = FactorGraph();
    fgb_bit = Bit();
    fgb_bit.Input = .2;

    fgc = FactorGraph();
    fgc_bit = Bit();
    fgc_bit.Input = .4;


    i = 1;
    fg.NumSteps = 0;
    while fg.hasNext()
        fg.solve(false);

        nextbBit = Bit();
        nextcBit = Bit();

        fgb.addFactor(@xorDelta,fgb_bit,nextbBit);
        fgc.addFactor(@xorDelta,fgc_bit,nextcBit);

        nextbBit.Input = .2;
        nextcBit.Input = .4;

        bbelief = b.get(1).Belief;
        cbelief = c.get(1).Belief;

        fgb.solve();
        fgc.solve();

        bbeliefexp = fgb_bit.Belief;
        cbeliefexp = fgc_bit.Belief;
        assertElementsAlmostEqual(bbeliefexp,bbelief(2));
        assertElementsAlmostEqual(cbeliefexp,cbelief(2));

        fg.advance();
        i = i+1;

        fgb_bit = nextbBit;
        fgc_bit = nextcBit;
    end


    %Test two streams with one variables source
    in1 = [.8 .2];
    in2 = [.7 .3];
    v = Bit();
    ng1 = FactorGraph(v);
    ng1.addFactor(@constFactor,v,in1);

    v = Bit();
    ng2 = FactorGraph(v);
    ng2.addFactor(@constFactor,v,in2);

    s = BitStream();
    data = repmat([.6 .4]',1,20);
    s.DataSource = DoubleArrayDataSource(data);

    fg = FactorGraph();
    fg.addFactor(ng1,s);
    fg.addFactor(ng2,s);

    fg2 = FactorGraph();
    b = Bit();
    fg2.addFactor(@constFactor,b,in1);
    fg2.addFactor(@constFactor,b,in2);
    b.Input = .4;
    fg2.solve();
    %b.Belief

    fg.initialize();

    s.get(1).Name = 'firstVar';
    %s.LastVar.Name = 'lastVar';
    fg.NumSteps = 0;
    while fg.hasNext()
        fg.solve(false);        
        assertElementsAlmostEqual(s.get(1).Belief(2),b.Belief);        
        fg.advance();
    end

    %TODO: should I test different buffer sizes?

end
