
Instructions:
1) execute the installer by typing python build.py
   optionally you can do "build.py -t" if you want tests to run
2) The resulting zip file will reside in this directory and will be called
   dimple_v<version num>.zip
3) The zip file can be copied anywhere and users will simply have to
   execute the startup.m file when running from MATLAB

Modifying the kit:
There are four main text files that can be used to modify the kit:
-files_to_exclude.txt - These are the files not to be included in the kit.
		      Users can specify directories or files.
-regex_to_exclude.txt - Any file that matches one of the regular expressions
		      in this file will not be included in the kit.
-mfiles_to_keep.txt - PFiles will not be generated for
		    any mfiles that match a file name in this file
		    or are in a subdirectory of a directory specified in this
		    file.
-mfiles_not_to_copyright.txt - The installer automatically adds a 
			     copyright to all files.  This file indicates
			     which directories and files this should not 
			     apply to.  All open source libraries should
			     be included here so that we do not modify
			     open source files.

