function testFactorTable

    for i = 1:2
        if i == 1
            dd = DiscreteDomain({'rain','no rain'});
        else
            dd = {'rain','no rain'};
        end

        %test FactorTable(indices,values,domains);
        indices = [0 0; 1 1];
        values = [.8; .2];
        ft = FactorTable(indices,values,dd,dd);
        fg = FactorGraph();
        d = Discrete(dd,2,1);
        f = fg.addFactor(ft,d);
        assertEqual(f.FactorTable.Indices,indices);
        assertEqual(f.FactorTable.Weights,values);


        %test errors?

        %test FactorTable (values, domains);
        values = [.8 .3; ...
                  .2 .7];
        fg = FactorGraph();
        ft = FactorTable(values,dd,dd);
        d = Discrete(dd,2,1);
        f = fg.addFactor(ft,d);
        assertEqual(f.FactorTable.get('rain','rain'),.8);
        assertEqual(f.FactorTable.get('no rain','rain'),.2);
        assertEqual(f.FactorTable.get('rain','no rain'),.3);
        assertEqual(f.FactorTable.get('no rain','no rain'),.7);

        %test errors

        %test FactorTable(domains);
        ft = FactorTable(dd,dd);

        %test errors

        %ft.set(blah);
        ft.set('rain','no rain',.1);
        ft.set('no rain','rain',.9);
        ft.set('rain','rain',.3);
        ft.set('no rain','rain',.7);
        assertEqual(ft.get('rain','no rain'),.1);
        fg = FactorGraph();
        d = Discrete(dd,2,1);
        fg.addFactor(ft,d);

        %test errors

        %test ft.set({{},{},{}});
        ft.set({'rain','no rain',.2},...
               {'no rain','no rain',.8});

        assertEqual(ft.get('no rain','no rain'),.8);

        %test errors

        %test re-use of FactorTables
        ft = FactorTable([0 0; 1 1],[.8 .2],dd,dd);
        fg = FactorGraph();
        d = Discrete(dd,2,1);
        f1 = fg.addFactor(ft,d);
        f2 = fg.addFactor(ft,d);
        f1.FactorTable.Weights = [.6 .4];
        assertEqual(f2.FactorTable.Weights,[.6; .4]);

        %%%%%%% Do all that stuff directly with addFactor


        %test FactorTable(indices,values,domains);
        fg = FactorGraph();
        d = Discrete(dd,2,1);
        indices = [0 0; 1 1];
        values = [.8; .2];
        f = fg.addFactor(indices,values,d);
        assertEqual(f.FactorTable.Indices,indices);
        assertEqual(f.FactorTable.Weights,values);

        %test errors

        %test FactorTable (values, domains);
        fg = FactorGraph();
        d = Discrete(dd,2,1);
        values = [.8 .3; ...
                  .2 .7];
        f = fg.addFactor(values,d);
        assertEqual(f.FactorTable.get('rain','rain'),.8);
        assertEqual(f.FactorTable.get('no rain','rain'),.2);
        assertEqual(f.FactorTable.get('rain','no rain'),.3);
        assertEqual(f.FactorTable.get('no rain','no rain'),.7);
    end
    %test errors
end
