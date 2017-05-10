package mlab04;

import java.util.HashMap;

public class FileProcess {

	public void uploadFile(Network network, String fileName, long owner) {
		String fileID = owner + fileName;
		network.uploadFile(fileName, new PublicFile(owner, fileID));
	}

	public void deleteFile(Network network, String fileName, long owner) {
		System.out.println("FILE ID: " + network.getFileID(fileName));

		String id = owner + fileName;

		if (network.getFileID(fileName).equals(id))
			try {
				network.deleteFile(fileName);
			} catch (NullPointerException e) {
				System.out.println("No file named " + fileName);
			}
		else
			System.out.println("You do not have the privilege to delete this file.");
	}

	public boolean fileExists(Network network, String fileName) {
		if (network.getFiles().get(fileName) != null) {
			return true;
		}
		return false;
	}

	public void transferFiles(Network prev, Network newSucc) {
		try {
			HashMap<String, PublicFile> newFiles = prev.getFiles();
			newSucc.setNewFiles(newFiles);
		} catch (NullPointerException e) {
			System.out.println("No files just yet.");
		}
	}
}
