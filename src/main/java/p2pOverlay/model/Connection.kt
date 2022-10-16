package p2pOverlay.model

import java.io.Serializable
import java.net.InetSocketAddress
import java.util.BitSet;

data class Connection(var numericID : BitSet, var address : InetSocketAddress) : Serializable {
}