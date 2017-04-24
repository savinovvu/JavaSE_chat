package ru.inbox.savinov_vu.client;


import ru.inbox.savinov_vu.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }


    public static void main(String[] args) {
        new BotClient().run();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] split = message.split(": ");
            if (split.length != 2) return;
            String userAnswerPart ="Информация для "+split[0]+": ";
            String textMessage = split[1];

            if ("дата".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("d.MM.YYYY", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
            }

            if ("день".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("d", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("месяц".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("MMMM", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("год".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("YYYY", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("время".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("H:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("час".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("H", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("минуты".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("m", Locale.ENGLISH).format(Calendar.getInstance().getTime()));

            }

            if ("секунды".equals(textMessage)) {
                sendTextMessage(userAnswerPart + new SimpleDateFormat("s", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
            }

        }
    }
}
