package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection implements Runnable {

    private final PrintStream clientOutput;
    private final BufferedReader clientInput;
    private ChatServer chatServer;
    private ServerSocket serverSocket;
    private String clientName;
    private boolean isPrivate = false;

    public void setPrivateConversationList(List<ClientConnection> privateConversationList) {
        this.privateConversationList = privateConversationList;
    }

    private List<ClientConnection> privateConversationList = new ArrayList<>();

    public ClientConnection(ChatServer chatServer, Socket socket, ServerSocket server, String clientName) throws IOException {
        this.clientName = clientName;
        this.serverSocket = server;
        this.chatServer = chatServer;
        this.clientOutput = new PrintStream(socket.getOutputStream(), true, "UTF8");
        this.clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
    }

    public List<ClientConnection> getPrivateConversationList() {
        return privateConversationList;
    }

    @Override
    public void run() {
        String line = null;
        while(true) {
           try {
               if ((line = clientInput.readLine()) == null) {
                   break;
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
           chatServer.gotMessage(line, this, serverSocket);
           if(line.equals("shutdown") || line.equals("quit")) {
               break;
           }
        }
        try {
            clientInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientOutput.close();
   }

    public void sendMessage(String message) {
        clientOutput.println(message);
    }

    public String getClientName() {
        return clientName;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
