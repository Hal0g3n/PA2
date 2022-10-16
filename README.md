# PA2
CS5132 Programming Assignment 2 on Distributed Peer Trees (DPTree).

This repo provides an (incomplete) implementation of DPTrees, a balanced tree data structure that aims to assist with faster multidimensional queries in P2P systems by distributing parts of the tree across different peers. Our Distributed Peer Tree will be created based on 2 other data structures:
- Skip Graphs: The overlay that manages the peers, chosen for its absolute distance-agnostic nature in organizing the peers. The implementation for the skip graphs and other p2p network functions, including the simple UI demonstration, can be found in src/java/p2pOverlay. 
- R-Trees: The balanced tree of choice in this implementation due to its ability to handle searches, insertions and deletions with multidimensional queries while also having a balancing mechanism. The implementations for the RTree and its associated functions and classes can be found in the src/java/model folder.

The main applications of the DPTree is to assist with faster multidimensional queries across different devices.
This is useful in the context of Peer to Peer (P2P) services, such as in P2P energy trading. Speeding up queries will increase the efficiency of the overall system and improve the experience in general. 

We have made a simple UI as a proof that the P2P connection works and to give an idea of how this structure will speed up P2P services.
