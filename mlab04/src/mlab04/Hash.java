package mlab04;

public class Hash {

	public String SDBMHash(String str) {
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}

		return Long.toString(hash);
	}
}