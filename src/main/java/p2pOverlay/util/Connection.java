package p2pOverlay.util;

import java.net.InetSocketAddress;

public record Connection(int id, InetSocketAddress address) { }
