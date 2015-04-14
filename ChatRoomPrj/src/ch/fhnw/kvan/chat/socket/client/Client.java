package ch.fhnw.kvan.chat.socket.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import ch.fhnw.kvan.chat.general.ChatRoom;
import ch.fhnw.kvan.chat.general.ChatRoomDriver;
import ch.fhnw.kvan.chat.gui.ClientGUI;
import ch.fhnw.kvan.chat.interfaces.IChatRoom;
import ch.fhnw.kvan.chat.utils.In;
import ch.fhnw.kvan.chat.utils.Out;

public class Client implements Runnable, IChatRoom {

	private static Logger log;
	private static ClientGUI gui;

	private In inputStream;
	private Out outputStream;

	private String name;
	private Socket socket;

	private HashSet<String> participantList = new HashSet<String>();
	private HashSet<String> topics = new HashSet<String>();
	private HashMap<String, List<String>> messages = new HashMap<String, List<String>>();

	public static void main(String[] args) throws IOException {
		log = Logger.getLogger(Client.class);

		Client client = new Client(args[0], args[1], Integer.parseInt(args[2]));

		client.login();
		client.startReadingInput();

	}

	public Client(String name, String host, int port) {
		log.info("create client " + name);

		this.name = name;

		try {
			socket = new Socket(host, port, null, (int) Math.random() * 65000);
		} catch (IOException e) {
			e.printStackTrace();
		}

		inputStream = new In(socket);
		outputStream = new Out(socket);

		gui = new ClientGUI(this, name);
	}

	public void login() {
		log.info(name + ": login");
		outputStream.println("name=<" + name + ">");
	}

	public void startReadingInput() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		String input = inputStream.readLine();

		while (input != null) {

			log.info("input: " + input);

			String[] i = input.split("=");

			if (i.length == 2)
				handleInput(i[0], i[1]);

			input = inputStream.readLine();
		}
	}

	private void handleInput(String key, String value) {
		try {

			switch (key) {

			case "add_participant":
				log.info("Add new participant "
						+ value.substring(1, value.length() - 1));
				addParticipant(value.substring(1, value.length() - 1));
				break;

			case "participants":
				log.info("Add participants " + value);
				String[] participants = value.split(";");
				for (String p : participants) {
					addParticipant(p);
				}
				break;

			case "topics":
				log.info("Add topics " + value);
				String[] tops = value.split(";");
				for (String t : tops) {
					topics.add(t);
					messages.put(t, new ArrayList<String>());
					gui.addTopic(t);
				}
				break;

			case "messages":
//				String msgsString = value.substring(1, value.indexOf(">")-1).split(":");
//				String topi = ii[0];
//				String[] msgs = ii[1].split(";");
//				log.info("Add messages to " + topi);
//				for (String m : msgs) {
//					messages.get(topi).add(m);
//					if (gui.getCurrentTopic().equals(topi))
//						gui.updateMessages(msgs);
//				}
				break;

			case "add_topic":
				String topic = value.substring(1, value.length() - 1);
				log.info("Add topic " + topic);
				if (!topics.contains(topic)) {
					topics.add(topic);
					messages.put(topic, new ArrayList<String>());
					gui.addTopic(topic);
				}
				break;

			case "message":
				String[] i = value.substring(1, value.length() - 1).split(";");
				String top = i[0];
				String msg = i[1];
				log.info("Add message " + msg + " to topic " + top);

				if (topics.contains(top)) {
					messages.get(top).add(msg);
					if (top.equals(gui.getCurrentTopic()))
						gui.addMessage(msg);
				}
				break;

			case "remove_name":
				String name = value.substring(1, value.length() - 1);
				log.info("remove participant " + name);
				participantList.remove(name);
				gui.removeParticipant(name);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean addParticipant(String name) throws IOException {
		if (!this.name.equals(name)) {
			participantList.add(name);
			gui.addParticipant(name);
			return true;
		} else
			return false;
	}

	@Override
	public boolean removeParticipant(String name) throws IOException {
		outputStream.println("remove_name=<" + name + ">");
		return true;
	}

	@Override
	public boolean addTopic(String topic) throws IOException {
		if (!topics.contains(topic)) {
			topics.add(topic);
			messages.put(topic, new ArrayList<String>());
			gui.addTopic(topic);
			outputStream.println("add_topic=<" + topic + ">");
			return true;
		}
		return false;

	}

	@Override
	public boolean removeTopic(String topic) throws IOException {
		topics.remove(topic);
		outputStream.println("remove_topic=<" + topic + ">");
		return false;
	}

	@Override
	public boolean addMessage(String topic, String message) throws IOException {
		if (topics.contains(topic)) {
			gui.addMessage(message);
			messages.get(topic).add(message);
			outputStream.println("message=<" + topic + ";" + message + ">");
			return true;
		}
		return false;
	}

	@Override
	public String getMessages(String topic) throws IOException {
//		if (topics.contains(topic)) {
//			List<String> msgs = messages.get(topic);
//			int i = 0;
//			if (msgs.size() >= 10)
//				i = msgs.size() - 10;
//			List<String> submsgs = msgs.subList(i, msgs.size());
//			gui.updateMessages(submsgs.toArray(new String[submsgs.size()]));
//		}

		outputStream.println("action=getMessages;topic=<" + topic + ">");
		return "";
	}

	@Override
	public String refresh(String topic) throws IOException {
		outputStream.println("action=refreshMessages;topic=<" + topic + ">");
		return "";
	}
}
