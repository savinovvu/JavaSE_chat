package ru.inbox.savinov_vu.server;

import ru.inbox.savinov_vu.Connection;
import ru.inbox.savinov_vu.ConsoleHelper;
import ru.inbox.savinov_vu.message.Message;
import ru.inbox.savinov_vu.message.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        connectionMap.values().forEach(v -> {
            try {
                v.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не удалось отправить сообщение");
            }
        });
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("введите номер порта - от 1000 до 65535 ");
        int portNumber = ConsoleHelper.readInt();


        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            ConsoleHelper.writeMessage("сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    new Handler(socket).start();
                }
                socket = null;
            }

        } catch (IOException e) {
            ConsoleHelper.writeMessage("ошибка");
        }

    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {

                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if (message.getType() != MessageType.USER_NAME) {
                    continue;
                }
                String name = message.getData();
                if ("".equals(name) || connectionMap.containsKey(name)) {
                    continue;
                }
                connectionMap.put(name, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return name;
            }

        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                if (!userName.equals(pair.getKey())) {
                    connection.send(new Message(MessageType.USER_ADDED, pair.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String data = message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, String.format("%s: %s", userName, data)));
                } else ConsoleHelper.writeMessage("ошибка");
            }
        }

        public void run() {
            String userName = null;
            ConsoleHelper.writeMessage(String.format("соединение с %s установлено", socket.getRemoteSocketAddress()));

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным сервером");

            } finally {
                if (Objects.nonNull(userName)) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("соединение с удаленным адресом закрыто");
            }

        }

    }


}
