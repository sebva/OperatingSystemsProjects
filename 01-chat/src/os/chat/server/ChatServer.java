package os.chat.server;

import os.chat.client.CommandsFromServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Vector;


/**
 * Each instance of this class is a server for one room.
 * In a first time, there is only one room server, and the names of the room available is fixed.
 * In a second time, you will have multiple room server, each managed by its own ChatServer.
 * A ChatServerManager will then be responsible for creating new rooms are they are added.
 */
public class ChatServer implements ChatServerInterface {

    /**
     * RMI registry prefix used by each ChatServer object
     */
    public static final String CHAT_SERVER_RMI_REG_PREFIX = "room_";
    /**
     * The name of the room hosted by this ChatServer
     */
    private final String roomName;
    /**
     * List of clients that want to be notified whenever a message is sent
     */
    private final Vector<CommandsFromServer> registeredClients;

    /**
     * Constructor: initializes the chat room and register it to the RMI registry
     *
     * @param roomName The name of the room hosted by this ChatServer
     */
    public ChatServer(String roomName) {
        this.roomName = roomName;
        this.registeredClients = new Vector<>();

        // Register the ChatServer to the RMI registry
        try {
            ChatServerInterface stub = (ChatServerInterface) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(CHAT_SERVER_RMI_REG_PREFIX + roomName, stub);
            System.out.println("Room " + roomName + " registered");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String message, String publisher) {
        /*
          Iterate over the registeredClients and send them the message.
          We use the Java Iterator syntax, because it's possible to remove an object
          while iterating with it.
         */
        for (Iterator<CommandsFromServer> iterator = registeredClients.iterator(); iterator.hasNext(); ) {
            CommandsFromServer client = iterator.next();
            try {
                // Add the name of the publisher to the message
                String messageToDisplay = publisher + "> " + message;
                client.receiveMsg(roomName, messageToDisplay);
            } catch (RemoteException e) {
                System.err.println("Client unreachable, removing...");
                iterator.remove();
            }
        }
    }

    @Override
    public void register(CommandsFromServer client) {
        registeredClients.add(client);
        System.out.println("Client registered");
    }

    @Override
    public void unregister(CommandsFromServer client) {
        registeredClients.remove(client);
        System.out.println("Client unregistered");
    }

}
