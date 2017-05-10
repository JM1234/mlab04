package mlab04;

import java.util.HashMap;

public class Network {

	long predecessor = -1;
	long successor = -1;
	long id;
	long predecessorIP;
	long successorIP;
	HashMap<String, PublicFile> files = new HashMap<String, PublicFile>();

	public Network(long id) {
		this.id = id;
	}

	public long getPredecessor() {
		return predecessor;
	}

	public long getSuccessor() {
		return successor;
	}

	public void setPredecessor(long predecessor) {
		this.predecessor = predecessor;
	}

	public void setSuccessor(long successor) {
		this.successor = successor;
	}

	public void setSuccessorIP(long successorIP) {
		this.successorIP = successorIP;
	}

	public void setPredecessorIP(long predecessorIP) {
		this.predecessorIP = predecessorIP;
	}

	public long getSuccessorIP() {
		return successorIP;
	}

	public long getPredecessorIP() {
		return predecessorIP;
	}

	public long getID() {
		return id;
	}

	public HashMap<String, PublicFile> getFiles() {
		return files;
	}

	// upload as keeper of file
	public void uploadFile(String fileName, PublicFile file) {
		files.put(fileName, file);
	}

	// delete if keeper of file wants to
	public void deleteFile(String key) {
		files.remove(key);
	}

	// if predecessor changed
	public void setNewFiles(HashMap<String, PublicFile> files) {
		this.files = files; // check if this will replace old hashmap
	}

	public String getFileID(String key) {
		return files.get(key).getID();
	}
}