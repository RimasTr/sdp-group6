sdp-group6
==========

University of Edinburgh - Software Design Project 2013 - Group 6

Setting up the java project
==========================

This project can be imported into Eclipse.
There are several steps that must be taken before the code can compile and run successfully.

1/ The leJOS NXJ plugin must be installed. Instruction for doing this can be found here:
http://lejos.sourceforge.net/nxt/nxj/tutorial/Preliminaries/UsingEclipse.htm

2/ Once you have installed the plugin, you must change a few settings.
	a) NXJ_HOME directory must be set to /*pathtoyourworkspace*/sdp-group6/lib/lejos_nxj_91
	b) Under connection type, choose 'Bluetooth'
	c) Check 'Connect to address' and enter: 00:16:53:08:A0:E6
	d) Check 'Connect to named brick' and enter: group6
	
3/ 
Right-click on the project folder in the Package Explorer (left hand side of Eclipse).
Go to 'Build Path > Add libraries'	
Now select 'LeJOS Library Container' and click 'Next'.
Select either platform and click finish.
Now repeat this but at the end select the other platform.

You the code should now compile with no problems.

4/ * IMPORTANT *
Now exit Eclipse.
Using the terminal, navigate to /*pathtoyourworkspace*/sdp-group6/ and run the eclipse.sh script.
This will launch Eclipse with the correct paths loaded and the bluetooth code should run as well as compile now.

E.g.
$ cd
$ cd /*pathtoyourworkspace*/sdp-group6/
$ ./eclipse.sh

5/ Eclipse is now running and all code should be working.
If not, please let me know asap!

	- Jack