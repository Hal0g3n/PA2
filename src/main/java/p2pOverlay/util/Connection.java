package p2pOverlay.util;

import p2pOverlay.Peer;

import java.net.InetSocketAddress;

public record Connection(Peer peer, InetSocketAddress address) { }
