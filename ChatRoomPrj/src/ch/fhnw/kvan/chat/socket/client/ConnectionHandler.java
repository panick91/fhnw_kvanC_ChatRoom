package ch.fhnw.kvan.chat.socket.client;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import ch.fhnw.kvan.chat.general.ChatRoom;
import ch.fhnw.kvan.chat.interfaces.IChatRoom;
import ch.fhnw.kvan.chat.utils.In;
import ch.fhnw.kvan.chat.utils.Out;

public class ConnectionHandler implements Runnable {

	private static Logger log;

	private final Socket client;
	private ConnectionListener listener;
	private In in;
	private Out out;

	private String name;
	private String message;

	public ConnectionHandler(Socket s, ConnectionListener cl) {

		log = Logger.getLogger(ConnectionHandler.class);
		log.info("ConnectionHandler initialized");

		client = s;
		listener = cl;

		listener.registerConnectionHandler(this);
	}

	@Override
	public void run() {

		in = new In(client);
		out = new Out(client);

		String input = in.readLine();
		while (input != null && input != "") {
			setMessage(input);
			input = in.readLine();
		}
	}

	public synchronized void setName(String n) {
		name = n;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setMessage(String msg) {
		message = msg;
	}

	public synchronized String getMessage() {
		String msg = message;
		message = null;
		return msg;
	}

	public void addParticipant(String name)  {
		out.println("add_participant=<" + name + ">");
	}

	public void sendParticipants(String participants) {
		out.println(participants);
	}

	public void removeParticipant(String name)  {
		out.println("remove_name=<" + name + ">");
	}

	public void addTopic(String topic)  {
		out.println("add_topic=<" + topic + ">");
	}

	public void removeTopic(String topic)  {
		out.println("remove_topic=<" + topic + ">");
	}

	public void addMessage(String topic, String message)  {
		out.println("message=<" + topic + ";" + message + ">");
	}

	public void sendMessages(String messages){
		out.println(messages);
	}

}
