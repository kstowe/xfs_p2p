# xfs_p2p

This project simulates a serverless peer-to-peer filesystem based on the classic [paper on xFS] (https://www.cs.iastate.edu/~cs652/notes/xfs.pdf) in which peers can share files directly with other peers. A user can store files locally and share them with others by sharing the contents of that directory with the tracking server.

The system is organized into two components: peer and tracking server. The tracking server does not store any files. While running, the tracking server will maintain a list of peers that are active in the network. It will also keep track of the files that each peer is able to provide. However, this state is not maintained persistently. This characteristic allows the server to recover easily if it crashes. 

The peers can download a file by providing a file name to the tracking server and asking which other peers have it. The peer will then select a peer according to a peer selection algorithm to download the file from. If the download succeeds, the peer updates the server that it can now provide that file. If the download fails, the peer will either retry the download from the same peer or try to download the file from a different peer.
