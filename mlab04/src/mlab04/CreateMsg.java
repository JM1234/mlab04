package mlab04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CreateMsg extends Thread {
	BroadcastMsg br;
	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

	public CreateMsg(BroadcastMsg br) {
		this.br = br;
	}

	public void run() {
		try {
			br.sendMSocket(stdIn.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}