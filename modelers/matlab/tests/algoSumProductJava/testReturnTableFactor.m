function testReturnTableFactor()

    %disp('++testReturnTableFactor');

    %First let's just add the factor as indices and values
    fg = FactorGraph();
    b = Bit(2,1);
    ind = [0 0; 1 1];
    val = [1 1];
    tf = fg.addFactor(ind,val,b);
    ct = tf.FactorTable;
    ind2 = ct.Indices;
    assertEqual(isequal(ind, ind2), true);
   	
    %Now let's add the Factor using createTable to test we can share tables
    fg = FactorGraph();
    b = Bit(2,1);   
    t = fg.createTable(ind,val,b.Domain,b.Domain);
    tf = fg.addFactor(t,b);
    ct = tf.FactorTable;
    ind2 = ct.Indices;
    assertEqual(isequal(ind, ind2), true);
    %disp('--testReturnTableFactor');
end
