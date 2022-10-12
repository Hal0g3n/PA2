ok so for now this is the idea

we have PeerService which will init a ConnectionService and init a Peer

Peers will have a hashmap of Connections which it is maintaining

we also need a "Connection" data class which handles the... other connection information
 -> skipgraph ID
 -> InetSocketAddress (ip, port)

afterwards we'll do the SkipGraph stuff but this for now

`#TODO: handle connection between two servers!!`