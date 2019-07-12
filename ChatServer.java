package networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static List<ClientConnection> clients = new ArrayList<>();
    private Integer counter = 0;

    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer();
        chatServer.run();
    }

    public void run() throws IOException {
        try(ServerSocket server = new ServerSocket(23)) {
            try {
                while (true) {
                    Socket socket = server.accept();
                    counter++;
                    String clientName = "Client #" + counter;
                    ClientConnection clientConnection = new ClientConnection(this, socket, server, clientName);
                    clients.add(clientConnection);
                    Thread thread = new Thread(clientConnection);
                    thread.start();
                }
            } catch (SocketException e) {
                System.out.println("Server was closed!");
            }
        }
    }

    public void gotMessage(String message, ClientConnection clientConnection, ServerSocket serverSocket) {
        if (message.equals("public")) {
            handleChangeToPublic(clientConnection);
        } else if (message.startsWith("private")) {
          handlePrivateConversation(message, clientConnection);
        } else if (message.equals("list")) {
            sendListToClient(clientConnection);
        } else if (message.startsWith("name:")) {
            handleNameChange(message, clientConnection);
        } else if (message.equals("quit")) {
            handleClientQuit(clientConnection);
        } else if (message.equals("shutdown")){
            handleShutdown(serverSocket);
        } else {
            if (clientConnection.isPrivate() && !clientConnection.getPrivateConversationList().isEmpty()) {
                routeMessagePrivately(message, clientConnection);
            } else {
                routeMessage(message, clientConnection);
            }
        }
    }

    private void handleChangeToPublic(ClientConnection clientConnection) {
        clientConnection.setPrivate(false);
        clientConnection.getPrivateConversationList().clear();
        clientConnection.sendMessage("Mode was changed back to public, all clients will get your messages now.");
    }

    private void routeMessagePrivately(String message, ClientConnection clientConnection) {
        for(ClientConnection client: clientConnection.getPrivateConversationList()) {
            client.sendMessage(clientConnection.getClientName() + " : " + message);
        }
    }

    private void handlePrivateConversation(String message, ClientConnection clientConnection) {
        clientConnection.setPrivate(true);
        List<ClientConnection> list;
        if(clientConnection.getPrivateConversationList().isEmpty()) {
            message = message.split("private")[1].trim();
            list = new ArrayList<>();

        } else {
            list = clientConnection.getPrivateConversationList();
        }

        for(ClientConnection client: clients) {
            if (client.getClientName().equals(message)) {
                list.add(client);
            }
        }
        if (list.isEmpty()) {
            clientConnection.sendMessage(message + " doesn't exists!!!");
        } else {
            clientConnection.setPrivateConversationList(list);
        }
    }

    private void sendListToClient(ClientConnection clientConnection) {
        clientConnection.sendMessage("Connected clients");
        for (ClientConnection client: clients) {
            if(!client.getClientName().equals(clientConnection.getClientName())) {
                clientConnection.sendMessage(client.getClientName());
            }
        }
    }

    private void routeMessage(String message, ClientConnection clientConnection) {
        for (ClientConnection client : clients) {
            if (clientConnection != client) {
                client.sendMessage(clientConnection.getClientName() + " : " + message);
            }
        }
    }

    private void handleNameChange(String message, ClientConnection clientConnection) {
        message = message.split("name:")[1].trim();
        for (ClientConnection client: clients) {
            if (client.getClientName().equals(message)) {
                clientConnection.sendMessage("Name is already taken! Please choose another name...");
                return;
            }
        }

        String oldName = clientConnection.getClientName();
        clientConnection.setClientName(message);
        for (ClientConnection client : clients) {
            if (clientConnection != client) {
                client.sendMessage(oldName + " renamed to " + message);
            }
        }
        clientConnection.sendMessage("Name successfully changed :)");

    }

    private void handleClientQuit(ClientConnection clientConnection) {
        clients.remove(clientConnection);

        for (ClientConnection client : clients) {
            if (clientConnection != client) {
                client.sendMessage(clientConnection.getClientName() + " has disconnected :(");
            }
        }
    }

    private void handleShutdown(ServerSocket serverSocket) {
        for (ClientConnection client : clients) {
            client.sendMessage("Server is shutting down...");
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}