1. The current code works for 5 nodes. (It can be changed by changing ids.txt file and 
initial coordinator node in the Main class)
2. The ids.txt file contains a single number which decremented as a node 
starts running. A node reads the number and declares it its own id, decrements it and 
writes back to the file. 
3. There are two method to run the code
	1. Normal (java Main) - if  you are starting a node first time use this one.
	   In this method, a node takes its id from ids.txt file
	2. Resume( java Main nodeID) - if you want to resume the crashed node
	   run the code with specifying the id of the node you want to resume

4. If you are starting all nodes for the first time, please make you that number in the ids.txt 
file is 5.

