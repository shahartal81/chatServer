package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient {
    public static void main(String[] args)  {
        try (Socket socket = new Socket("localhost", 23);
             BufferedReader socketInput = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), "UTF8"));
             PrintStream socketOutput = new PrintStream(
                     socket.getOutputStream(), true, "UTF8");
             BufferedReader commandLineInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("You are now connected :D");
            final AtomicBoolean isRunning = new AtomicBoolean(true);
            Thread threadScanner = new Thread(() -> {
                String line = "";
                while(isRunning.get()) {
                    try {
                        line = commandLineInput.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socketOutput.println(line);
                    if (line.equals("quit") || line.equals("shutdown")) {
                        isRunning.set(false);
                    }
                }
            });

            threadScanner.start();

            while(isRunning.get()) {
                try {
                    String line = socketInput.readLine();
                    if (line == null || line.equals("Server is shutting down")) {
                        isRunning.set(false);
                    }
                    System.out.println(line);
                } catch (IOException e) {

                }
            }

            threadScanner.join();
        } catch (InterruptedException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
