function result = myprogram()

    numheads = 0;

    weight = chimprand('weight');

    while 1
        name = sprintf('y%d',numheads);
        yn = chimpflip(name,weight);
        if yn == 0
            break
        end
        numheads = numheads + 1;
    end

    addChimpCost(-log(1/(abs(numheads-5)+1e-8)));

    result=zeros(2,1);
    result(1) = weight;
    result(2) = numheads;

end
