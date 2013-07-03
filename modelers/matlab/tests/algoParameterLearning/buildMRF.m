
function [fg,b,vertFT,horzFT] = buildMRF(H,W,vertWeights,horzWeights)

    fg = FactorGraph();
    b = Bit(H,W);

    vertFT = FactorTable(vertWeights,b.Domain,b.Domain);
    horzFT = FactorTable(horzWeights,b.Domain,b.Domain);

    if H > 1
        fg.addFactorVectorized(vertFT,b(1:end-1,:),b(2:end,:));
    end

    if W > 1
        fg.addFactorVectorized(horzFT,b(:,1:end-1),b(:,2:end));
    end

end