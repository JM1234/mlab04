package mlab04;

public class PublicFile {

	long owner;
	String id;
	String name;
	long keeper;

	public PublicFile(long owner, String id) {
		this.owner = owner;
		this.id = id;
	}

	public void setFileName(String name) {
		this.name = name;
	}

	public void setKeeper(long keeper) {
		this.keeper = keeper;
	}

	public String getID() {
		return id;
	}
}