function testCoreScheduler()
    %disp('++testCoreScheduler')
    d = com.analog.lyric.dimple.schedulers.DefaultScheduler();
    f = com.analog.lyric.dimple.schedulers.FloodingScheduler();
    s = com.analog.lyric.dimple.schedulers.SequentialScheduler();
    t = com.analog.lyric.dimple.schedulers.TreeOrFloodingScheduler();
    if ~strcmp('CSolver', class(getSolver()))
        fg = FactorGraph();
        fg.setScheduler(d);
        fg.setScheduler(f);
        fg.setScheduler(s);
        fg.setScheduler(t);    
    end
    %disp('--testCoreScheduler')
end
