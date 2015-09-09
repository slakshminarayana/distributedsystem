				      ==========
    					ReadMe
				      ==========

This program implenents a distributed system where it computes the sum of label values of all processes defined by the path specified in the config file for that process. This can be converted into an FTP by assigning a file to a process rather than a label.

This project uses SCTP protocol and can perform explicit routing by specifying the route in the configuration file. It also exercises termination. 

To compile the program, please use the command javac AOS_Project1.java

To run the program, please use the command java AOS_Project1 and the program needs to be run on all machines defined in the config file.

Note: The spacing between the hostname, port numbers and path defined in the config file should be exactly one tab. The space between nodes defined in the path should be exactly one space each.
