function modeler = getModeler()
    global g_dimpleModeler;
    if isempty(g_dimpleModeler)
        %g_dimpleModeler = ModelFactory();
        g_dimpleModeler = com.analog.lyric.dimple.matlabproxy.ModelFactory();
        g_dimpleModeler.setSolver(getSolver());
    end
    modeler = g_dimpleModeler;
end
