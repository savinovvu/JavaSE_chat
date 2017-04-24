package ru.inbox.savinov_vu.client;


import ru.inbox.savinov_vu.Connection;
import ru.inbox.savinov_vu.ConsoleHelper;
import ru.inbox.savinov_vu.message.Message;
import ru.inbox.savinov_vu.message.MessageType;

import java.io.IOException;
import java.net.Socket;

import static ru.inbox.savinov_vu.message.MessageType.*;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("введите ip адресс(или \"localhost\")");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт от 1000 (до 1000 порты часто заняты) до 65535");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("произошла ошибка отправки сообщения, соединение закрыто");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();
                if (clientConnected) {
                    ConsoleHelper.writeMessage("Соединение установлено.");
                } else {
                    ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
                }
                while (clientConnected) {
                    String string = ConsoleHelper.readString();
                    if ("exit".equals(string)) break;
                    if (shouldSendTextFromConsole()) {
                        sendTextMessage(string);
                    }
                }
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Произошло исключение");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        new Client().run();
    }


    public class SocketThread extends Thread {
        @Override
        public void run() {

            try {
                String serverAddress = getServerAddress();
                int serverPort = getServerPort();
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }


        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType type = message.getType();
                if (type == NAME_REQUEST) {
                    Message messageUserName = new Message(MessageType.USER_NAME, getUserName());
                    connection.send(messageUserName);
                    continue;
                }
                if (type == NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
                throw new IOException("Unexpected MessageType");
            }
        }


        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType type = message.getType();

                if (type == TEXT) {
                    processIncomingMessage(message.getData());
                    continue;
                }
                if (type == USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                    continue;
                }
                if (type == USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                    continue;
                }
                throw new IOException("Unexpected MessageType");


            }


        }
    }
}
