package mlab04;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class Peer {
	// private String ip = "192.168.1.1";
	private long myport;

	private BroadcastMsg broadcast;
	private UDPThread udp;
	private HashMap<Long, Network> network;

	public static void main(String[] args) {
		System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");
		System.out.println("//                      Type CREATE to create a network anytime.                       //");
		System.out.println("//                    Type 'JOIN <port_to_join>' to join a network.                    //");
		System.out.println("// Type 'PUBLISH/RETRIEVE/DELETE <network_you_joined_in>:<filename>' to process a file //");
		System.out.println("/////////////////////////////////////////////////////////////////////////////////////////");

		Peer peer = new Peer();
		peer.init();
	}

	public void init() {
		DatagramSocket udpSocket = null;
		InetAddress udpAddress = null;
		InetAddress mAddress = null;
		MulticastSocket msocket = null;

		try {
			msocket = new MulticastSocket(4445);
			// msocket.setInterface(InetAddress.getByName("172.16.6.166"));
			// //own ip = ip
			mAddress = InetAddress.getByName("230.0.0.1");
			msocket.joinGroup(mAddress);

			myport = 1024;// Long.parseLong(ip.replace(".",""));
			udpSocket = new DatagramSocket((int) myport);
			udpAddress = InetAddress.getByName("localhost");// ip where it is
															// waiting.

		} catch (IOException e) {
			e.printStackTrace();
		}

		network = new HashMap<Long, Network>();

		udp = new UDPThread(this, udpSocket, udpAddress, myport);
		udp.start();
		udp.setNetwork(network);

		String str = Keyword.BROADCAST_MSG + "10540182:" + myport; // ip on
																	// udpsocket
		broadcast = new BroadcastMsg(msocket, mAddress, udp, myport);
		broadcast.start();
		broadcast.setNetwork(network);
		broadcast.sendMSocket(str); // send a sign to self
	}

	public void updateNetwork(HashMap<Long, Network> network) {
		broadcast.setNetwork(network);
	}

}