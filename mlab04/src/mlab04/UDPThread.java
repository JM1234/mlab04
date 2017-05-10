package mlab04;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class UDPThread extends Thread {
	DatagramPacket packet;
	byte[] buf;
	String received;

	private Peer peer;
	private DatagramSocket socket;
	private InetAddress address;
	private FileProcess fileProcess;
	private HashMap<Long, Network> network;

	private long to;
	private long myport;
	private Hash hash;

	public UDPThread(Peer peer, DatagramSocket socket, InetAddress address, long myport) {
		this.peer = peer;
		this.socket = socket;
		this.address = address;
		this.myport = myport;

		hash = new Hash();
		fileProcess = new FileProcess();
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public void setNetwork(HashMap<Long, Network> network) {
		this.network = network;
	}

	public void run() {

		while (true) {
			byte[] buf = new byte[256];
			packet = new DatagramPacket(buf, buf.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			address = packet.getAddress();
			received = new String(packet.getData(), 0, packet.getLength());
			System.out.println("UDP Received msg: " + received);

			try {
				// if may mag join ha iya
				if (received.startsWith(Keyword.JOIN_STRING)) {
					received = received.replace(Keyword.JOIN_STRING, "");
					String[] parsed = received.split(":");
					long req = Long.parseLong(parsed[1]); // .replace(".", ""));
					long id = Long.parseLong(hash(myport));
					long reqid = Long.parseLong(hash(req));

					System.out.println("Getting network: " + id);
					if (network.get(id).getPredecessor() == -1) {
						// first peer

						// network.get(id).setSuccessor(reqid);
						// network.get(id).setSuccessorIP(req);
						updateSuccessor(network.get(id), reqid, req);

						fileProcess.transferFiles(network.get(id), network.get(reqid));

						to = packet.getPort();

						send(Keyword.FOUND_STRING + myport + ":" + myport, (int) to);
						send(Keyword.PREDECESSOR + myport + ":" + myport, (int) to);
					}

					else {
						searchSucc(parsed);
					}
				}

				else if (received.startsWith("PREDECESSOR")) {
					received = received.replace("PREDECESSOR ", "");
					String[] parsed = received.split(":");
					long curr = Integer.parseInt(parsed[0]);
					long req = Integer.parseInt(parsed[1]);

					// hash ids
					long currID = Long.parseLong(hash(curr));
					long reqID = Long.parseLong(hash(req));

					long oldPreID = network.get(currID).getPredecessor();
					long oldPreIP = network.get(currID).getPredecessorIP();

					if (oldPreID == -1) {
						// network.get(currID).setPredecessor(reqID);
						// network.get(currID).setPredecessorIP(req);
						updatePredecessor(network.get(currID), reqID, req);
					}

					else if (oldPreID < reqID) {
						to = oldPreIP;
						send(Keyword.FOUND_STRING + curr + ":" + req, (int) to);
						// network.get(currID).setPredecessor(reqID);
						// network.get(currID).setPredecessorIP(req);
						updatePredecessor(network.get(currID), reqID, req);
					}

					else if (oldPreID > reqID) {
						// network.get(currID).setPredecessor(reqID);
						// network.get(currID).setPredecessorIP(req);
						updatePredecessor(network.get(currID), reqID, req);
					}

					print(currID);
				} else if (received.startsWith(Keyword.FOUND_STRING)) {
					received = received.replace(Keyword.FOUND_STRING, "");
					String[] parsed = received.split(":");
					long curr = Integer.parseInt(parsed[0]);
					long req = Integer.parseInt(parsed[1]);

					// hash ids
					long myportID = Long.parseLong(hash(myport));
					long currID = Long.parseLong(hash(curr));
					long reqID = Long.parseLong(hash(req));

					long succ = network.get(currID).getSuccessor();
					long succID = Long.parseLong(hash(succ));

					if (parsed.length == 2) {
						if (succID <= reqID || succID <= myportID || reqID < succID) {
							to = req;
							// network.get(currID).setSuccessor(reqID);
							// network.get(currID).setSuccessorIP(req);
							updateSuccessor(network.get(currID), reqID, req);

							// transfer files from prev successor to new
							// successor
							fileProcess.transferFiles(network.get(reqID), network.get(succID));

							send("PREDECESSOR " + curr + ":" + myport, (int) to);
							print(currID);
						}
					} else {
						String str = Keyword.FOUND_STRING + curr + ":" + req;
						for (int i = 2; i < parsed.length - 1; i++) {
							str += ":" + parsed[i];
						}
						to = Integer.parseInt(parsed[parsed.length - 1]);
						send(str, (int) to);
					}
				} else if (received.startsWith(Keyword.SEARCH_SUCC)) {
					received = received.replace(Keyword.SEARCH_SUCC, "");
					String[] parsed = received.split(":");
					searchSucc(parsed);
				}

				//// File Related

				else if (received.startsWith(Keyword.PUBLISH)) {
					received = received.replace(Keyword.PUBLISH, "");
					String owner = hash(packet.getPort());
					String[] parsed = received.split(":");
					String curr_net = hash(Long.parseLong(parsed[0]));

					fileProcess.uploadFile(network.get(curr_net), parsed[1], Long.parseLong(owner));
				}

				else if (received.startsWith(Keyword.DELETE)) {
					received = received.replace(Keyword.DELETE, "");
					String[] parsed = received.split(":");
					String owner = hash(packet.getPort());
					String curr_net = hash(Long.parseLong(parsed[0]));

					fileProcess.deleteFile(network.get(curr_net), parsed[1], Long.parseLong(owner));
				} else if (received.startsWith(Keyword.RETRIEVE)) {
					received = received.replace(Keyword.RETRIEVE, "");
					String[] parsed = received.split(":");
					searchFile(parsed);
				} else if (received.contains(Keyword.FOUND_FILE) || received.contains(Keyword.FILE_NOT_FOUND)) {
					String[] parsed = received.split(":");

					if (Long.parseLong(parsed[parsed.length - 1]) == myport) {
						System.out.println(received);
					} else {
						to = network.get(hash(Integer.parseInt(parsed[0]))).getPredecessorIP();
						send(received, (int) to);
					}
				}
			} catch (NullPointerException e) {
				System.out.println("Network inaccessible.");
			}
			received = "";
		}
	}

	private void print(long curr) {
		System.out.print("NETWORK [" + network.get(curr).getID() + "] ");
		System.out.print(" PREDECESSOR: " + network.get(curr).getPredecessor());
		System.out.print(" SUCCESSOR: " + network.get(curr).getSuccessor());
		System.out.println("");
	}

	private void searchSucc(String[] parsed) {
		long currNetwork = Integer.parseInt(parsed[0]);
		long currNetworkID = Long.parseLong(hash(currNetwork));

		try {
			long networkSucc = network.get(currNetworkID).getSuccessor();
			long networkPre = network.get(currNetworkID).getPredecessor();
			long req = Integer.parseInt(parsed[1]);
			long dest = Integer.parseInt(parsed[parsed.length - 1]);

			long myportID = Long.parseLong(hash(myport));
			long reqID = Long.parseLong(hash(req));
			long networkPreIP = network.get(currNetworkID).getPredecessorIP();
			long networkSuccIP = network.get(currNetworkID).getSuccessorIP();

			if (reqID < myportID && reqID > networkPre) {
				String str = Keyword.FOUND_STRING + currNetwork + ":" + myport;
				for (int i = 1; i < parsed.length - 1; i++) {
					str += ":" + parsed[i];
				}
				to = dest;
				send(str, (int) to);
				to = networkPreIP;
				send(Keyword.FOUND_STRING + currNetwork + ":" + req, (int) to);
			}

			else if ((reqID > myportID && reqID <= networkSucc) || networkSucc < myportID) {
				String str = Keyword.FOUND_STRING + currNetwork + ":" + networkSuccIP;
				for (int i = 1; i < parsed.length - 1; i++) {
					str += ":" + parsed[i];
				}
				to = dest;
				send(str, (int) to);

				// set my succ to req
				to = myport;
				send(Keyword.FOUND_STRING + currNetwork + ":" + req, (int) to);
			} else {

				String str = Keyword.SEARCH_SUCC + currNetwork + ":" + req;
				for (int i = 1; i < parsed.length; i++) {
					str += ":" + parsed[i];
				}
				str += ":" + myport;
				to = networkSuccIP;
				send(str, (int) to);
			}
		} catch (NullPointerException e) {
			System.out.println("Network inaccessible.");
		}
	}

	private void searchFile(String[] parsed) {
		// parsed[1] == fileName
		// parsed[0] == network searching in
		// parsed[2] == requester

		String net = hash(Long.parseLong(parsed[0]));
		String str = "";

		if (fileProcess.fileExists(network.get(net), parsed[1])) {
			str = parsed[1] + Keyword.FOUND_FILE + parsed[0] + "with" + hash(myport);

			if (parsed.length == 2) {
				to = myport; // if local file
				str += ":" + myport;
			} else {
				to = network.get(net).getPredecessorIP();
				str += ":" + parsed[2];
			}
		} else {
			// parsed.length == 2 === sending to next succ
			// network.get(net).getPredecessorIP

			if (parsed.length == 2 || network.get(net).getSuccessorIP() != network.get(net).getPredecessorIP()) {
				str = Keyword.RETRIEVE + parsed[0] + ":" + parsed[1];

				// if greater than 2, requester is not me
				if (parsed.length > 2)
					str += ":" + parsed[2];
				// let them know i am the requester
				else
					str += ":" + myport;

				// pass to searching to next successor
				to = network.get(net).getSuccessorIP();
			}

			else if (network.get(net).getSuccessorIP() == network.get(net).getPredecessorIP()
					|| network.get(net).getSuccessorIP() == Integer.parseInt(parsed[2])) {

				to = network.get(net).getPredecessorIP();
				str = parsed[1] + " " + Keyword.FILE_NOT_FOUND + ":" + parsed[0] + ":" + parsed[2];
			}
		}
		send(str, (int) to);
	}

	public void send(String str, int to) {
		try {
			buf = str.getBytes();
			DatagramPacket packet1 = new DatagramPacket(buf, buf.length, address, (int) to);
			socket.send(packet1);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e3) {
			e3.printStackTrace();
		}
	}

	String hash(long ip) {
		return hash.SDBMHash(Long.toString(ip));
	}

	private void updateSuccessor(Network net, long newSucc, long ip) {
		net.setSuccessor(newSucc);
		net.setSuccessorIP(ip);
		peer.updateNetwork(network);
	}

	private void updatePredecessor(Network net, long newPre, long ip) {
		net.setPredecessor(newPre);
		net.setPredecessorIP(ip);
		peer.updateNetwork(network);
	}
}
