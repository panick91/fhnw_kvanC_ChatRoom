package ch.fhnw.kvan.chat.socket.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import ch.fhnw.kvan.chat.general.ChatRoom;
import ch.fhnw.kvan.chat.general.ChatRoomDriver;

// 
public class Server {

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();

		ChatRoomDriver chatRoomDriver = new ChatRoomDriver();
		chatRoomDriver.connect("localhost", 1235);

		ServerSocket server = new ServerSocket(1235);
		ConnectionListener listener = new ConnectionListener((ChatRoom)chatRoomDriver.getChatRoom());
		listener.start();

		while (true) {
			Socket s = server.accept();
			
			Thread t = new Thread(new ConnectionHandler(s, listener));
			t.start();			
		}
	}
}
