package ru.inbox.savinov_vu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        String s = null;
        while (s == null) {
            try {
                s = reader.readLine();
            } catch (IOException e) {
                writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте ещё раз.");

            }
        }
        return s;
    }

    public static int readInt() {
        Integer n = null;

        while (n == null) {
            try {
                n = Integer.valueOf(readString());
            } catch (NumberFormatException e) {
                writeMessage("Произошла ошибка при попытке ввода числа. Попробуйсте ещё раз.");
            }
        }
        return n;
    }


}
