package mlab04;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class BroadcastMsg extends Thread {
	private MulticastSocket msocket;
	private InetAddress mAddress;
	private UDPThread udp;
	private Hash hash;

	byte[] buf = new byte[256];
	DatagramPacket packet;
	int receivedPort;
	private boolean didCreate = false;
	private HashMap<Long, Network> network;

	long myport;
	long to;
	CreateMsg create;

	public BroadcastMsg(MulticastSocket msocket, InetAddress mAddress, UDPThread udp, long myport) {
		this.msocket = msocket;
		this.mAddress = mAddress;
		this.udp = udp;
		this.myport = myport;

		hash = new Hash();
		System.out.println("HASH: " + hash(myport));
		create = new CreateMsg(this);
		create.start();
	}

	public void setNetwork(HashMap<Long, Network> network) {
		this.network = network;
	}

	public void run() {
		byte buf[] = new byte[256];

		while (true) {

			packet = new DatagramPacket(buf, buf.length);

			try {
				msocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String received = new String(packet.getData(), 0, packet.getLength());

			if (received.startsWith(Keyword.BROADCAST_MSG)) {
				System.out.println("Received message: " + received);
				String str[] = received.replace(Keyword.BROADCAST_MSG, "").split(":");
				receivedPort = Integer.parseInt(str[1].replace(".", ""));

				if (didCreate == true && receivedPort != myport) {
					sendMSocket(Keyword.CREATED_NETWORK + myport);
				} else if (receivedPort == myport) {
					CreateMsg create = new CreateMsg(this);
					create.start();
				}
			}

			else if (received.startsWith(Keyword.CREATED_NETWORK)) {
				String str = received.replace(Keyword.CREATED_NETWORK, "");

				receivedPort = Integer.parseInt(str.replace(".", ""));

				if (!didJoin() && receivedPort != myport) {

					System.out.println("Received message: " + received);

				}
			}

			// publish?
		}
	}

	public void sendMSocket(String str) {
		System.out.println("@sendMSocket");

		// add my created network to list
		if (str.equalsIgnoreCase("CREATE") && didCreate == false) {
			didCreate = true;

			long id = hash(myport);
			network.put(id, new Network(id));
			network.get(id).setPredecessor(-1);
			network.get(id).setSuccessor(id);
			network.get(id).setSuccessorIP(myport);
			udp.setNetwork(network);
			// initially setting parameters
			System.out.println("PREDECESSOR: nil");
			System.out.println("SUCCESSOR: " + id);

			str = Keyword.CREATED_NETWORK + myport;
			System.out.println("ADDED: " + network.get(id));
			System.out.println("[create] ADDED NETWORK: " + id);
		}

		if (str.startsWith(Keyword.BROADCAST_MSG) || str.startsWith(Keyword.CREATED_NETWORK)) {

			buf = str.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, mAddress, 4445);

			try {
				msocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		else if (str.startsWith(Keyword.JOIN_STRING)) {
			String str1 = str.replace(Keyword.JOIN_STRING, "");
			long receivedIP = Integer.parseInt(str1);
			long id = hash(receivedIP);
			network.put(id, new Network(id));
			udp.setNetwork(network);

			System.out.println("ADDED: " + network.get(hash(id)));
			System.out.println("[join] ADDED NETWORK: " + id);

			// send join to port
			to = receivedIP;

			try {
				udp.send(Keyword.JOIN_STRING + receivedIP + ":" + myport, (int) to);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		}

		else if (str.startsWith(Keyword.PUBLISH) || str.startsWith(Keyword.DELETE)) {
			String str1[] = null;

			if (str.startsWith("PUBLISH"))
				str1 = str.replace("PUBLISH ", "").split(":");
			else if (str.startsWith("DELETE"))
				str1 = str.replace("DELETE ", "").split(":");

			// String id = hash(Long.parseLong(str1[0]));
			to = (int) network.get(hash(Long.parseLong(str1[0]))).getSuccessorIP();
			udp.send(str, (int) to);
		}

		else if (str.startsWith(Keyword.RETRIEVE)) {
			to = (int) myport; // search local file first
			udp.send(str, (int) to);

		}

	}

	public boolean didJoin() {
		long id = hash(receivedPort);
		if (network.get(id) != null)
			return true;
		return false;
	}

	Long hash(long ip) {
		return Long.parseLong(hash.SDBMHash(Long.toString(ip)));
	}
}