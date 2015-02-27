package os.chat.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import os.chat.client.CommandsFromServer;


/**
 * Each instance of this class is a server for one room.
 * In a first time, there is only one room server, and the names of the room available is fixed.
 * In a second time, you will have multiple room server, each managed by its own ChatServer.
 * A ChatServerManager will then be responsible for creating new rooms are they are added. 
 */
public class ChatServer implements ChatServerInterface {

    private static final String CHAT_SERVER_RMI_REG_PREFIX = "room_";
    private String roomName;
	private Vector<CommandsFromServer> registeredClients;
    private Registry registry;
	
	/**
	 * Constructor: initializes the chat room and register it to the RMI registry
	 * @param roomName
	 */
	public ChatServer(String roomName){
		this.roomName = roomName;
		registeredClients = new Vector<>();

        // Register the ChatServer to the RMI registry
        try {
            ChatServerInterface stub = (ChatServerInterface) UnicastRemoteObject.exportObject(this, 0);
            registry = LocateRegistry.getRegistry();
            registry.rebind(CHAT_SERVER_RMI_REG_PREFIX + roomName, stub);
            System.out.println("Room " + roomName + " registered");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
	}
	
	public void publish(String message, String publisher) {
		
		System.err.println("TODO: publish is not implemented");
		
		/*
		 * TODO send the message to all registered clients
		 */
	}

	public void register(CommandsFromServer client) {
		registeredClients.add(client);
		System.out.println("Client registered");
	}

	public void unregister(CommandsFromServer client) {
        registeredClients.remove(client);
        System.out.println("Client unregistered");
	}
	
}
