function v = matlabVersion
    vvv = version('-release');
	v = sscanf(vvv,'%d%c');
end
