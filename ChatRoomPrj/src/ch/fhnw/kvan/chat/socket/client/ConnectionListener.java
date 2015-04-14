package ch.fhnw.kvan.chat.socket.client;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ch.fhnw.kvan.chat.general.ChatRoom;
import ch.fhnw.kvan.chat.interfaces.IChatRoom;

// Manages all active socket-connections to clients
public class ConnectionListener extends Thread {

    private static Logger log;

    private ChatRoom chatRoom;

    private CopyOnWriteArrayList<ConnectionHandler> connections;

    public ConnectionListener(ChatRoom cr) {
        log = Logger.getLogger(ConnectionListener.class);
        chatRoom = cr;
        connections = new CopyOnWriteArrayList<ConnectionHandler>();
    }

    public void registerConnectionHandler(ConnectionHandler handler) {
        connections.add(handler);
    }

    public synchronized ChatRoom getChatRoom() {
        return chatRoom;
    }

    @Override
    public void run() {
        while (true) {
            for (ConnectionHandler connection : connections) {

                String message = connection.getMessage();
                if (message != null)
                    handleMessage(message, connection);
            }
        }
    }

    private void handleMessage(String message, ConnectionHandler ch) {

        String key;
        String value;

        if (message.startsWith("action")) {
            String[] tasks = message.split(";");
            key = tasks[0].split("=")[1];
            value = tasks[1].split("=")[1].substring(1, tasks[1].split("=")[1].length() - 1);
        } else {
            String[] key_value = message.split("=");
            key = key_value[0];
            value = key_value[1].substring(1, key_value[1].length() - 1);
        }

        try {
            switch (key) {

                case "name":

                    ch.setName(value);
                    ch.sendParticipants(chatRoom.getParticipants());

                    chatRoom.addParticipant(value);

                    for (ConnectionHandler connection : connections) {
                        if (connection != ch) {
                            connection.addParticipant(value);
                        }
                    }
                    break;

                case "add_topic":
                    chatRoom.addTopic(value);
                    for (ConnectionHandler connection : connections) {
                        if (connection != ch)
                            connection.addTopic(value);
                    }
                    break;

                case "message":
                    String[] i = value.split(";");
                    chatRoom.addMessage(i[0], i[1]);
                    for (ConnectionHandler connection : connections) {
                        if (connection != ch)
                            connection.addMessage(i[0], i[1]);
                    }
                    break;

                case "remove_name":
                    chatRoom.removeParticipant(value);
                    connections.remove(ch);
                    for (ConnectionHandler connection : connections) {
                        connection.removeParticipant(value);
                    }
                    break;

                case "remove_topic":
                    chatRoom.removeTopic(value);
                    for (ConnectionHandler connection : connections) {
                        if (connection != ch)
                            connection.removeTopic(value);
                    }
                    break;

                case "getMessages":
                    String messages = chatRoom.getMessages(value);
                    ch.sendMessages(messages);
                    break;

                case "refreshMessages":
                    String messages2 = chatRoom.getMessages(value);
                    ch.sendMessages(messages2);
                    break;

                default:
                    log.error("The key " + key + " cannot be handeled.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
