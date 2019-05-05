# xfs_p2p

This projext simulates a peer-to-peer network for downloading files. The system is organized into two components: peer and tracking server. While running, the tracking server will maintain a list of peers that are active in the network. It will also keep track of the files that each peer is able to provide. However, this state is not maintained persistently. If the tracking server goes down and comes back up, it must rely on the peers in the network to restore its state.

The peers can download a file by providing a file name to the tracking server and asking which other peers have it. The peer will then select a peer according to a peer selection algorithm to download the file from. If the download succeeds, the peer updates the server that it can now provide that file. If the download fails, the peer will either retry the download from the same peer or try to download the file from a different peer.
