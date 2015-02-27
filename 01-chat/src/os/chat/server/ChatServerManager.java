package os.chat.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;


/**
 * This class manages the available ChatServers and available rooms.
 * In a first time, you should not modify its functionalities but only export them for being called by the ChatClient.
 * In a second time, you will modify this to allow creating new rooms and looking them up from the client.
 *
 */
public class ChatServerManager implements ChatServerManagerInterface {

    public static final String CHAT_SERVER_MANAGER_RMI_REG = "ChatServerManager";
    private Vector<String> chatRoomsList;
	
	private Vector<ChatServer> chatRooms;

    private Registry registry;
	
	/**
	 * Constructor of the ChatServerManager.
	 * Must export its functionalities to be called from RMI by the client.
	 */
	public ChatServerManager () {
		// initial: we create a single chat room and the corresponding ChatServer
        chatRooms = new Vector<ChatServer>() {
            {
                add(new ChatServer("sports"));
            }};
		chatRoomsList = new Vector<String>() {
            {
                add("sports");
            }};


        // Register to the RMI registry
        try {
            ChatServerManagerInterface stub = (ChatServerManagerInterface) UnicastRemoteObject.exportObject(this, 0);
            registry = LocateRegistry.getRegistry();
            registry.rebind(CHAT_SERVER_MANAGER_RMI_REG, stub);
            System.out.println("Server started");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

	}

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        new ChatServerManager();
    }

	public Vector<String> getRoomsList() throws RemoteException {
        // Return a copy, otherwise the client crashes
		return new Vector<>(chatRoomsList);
	}

	public boolean createRoom(String roomName) {
		
		System.err.println("server manager method createRoom not implemented.");
		
		/*
		 * TODO add the code to create a new room
		 */
		
		return false;
	}	
	
}
