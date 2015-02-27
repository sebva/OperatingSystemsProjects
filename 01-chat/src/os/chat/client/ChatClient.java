package os.chat.client;


import os.chat.server.ChatServerManager;
import os.chat.server.ChatServerManagerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;


public class ChatClient implements CommandsFromWindow,CommandsFromServer {

	/**
	 * The name of the user of this client
	 */
	private String userName;
	
	/**
	 * The graphical user interface, accessed through its interface.
	 * In return, the GUI will use the CommandsFromWindow interface to call methods to the ChatClient implementation.
	 */
	private final CommandsToWindow window ;

    private Registry registry;
    private ChatServerManagerInterface server;
	
	/**
	 * Constructor for the ChatClient. Must perform the connection to the server. If the connection is not successful, it must exit with an error.
	 * @param window
	 */
	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;

        try {
            registry = LocateRegistry.getRegistry();
            server = (ChatServerManagerInterface) registry.lookup(ChatServerManager.CHAT_SERVER_MANAGER_RMI_REG);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

	/*
	 * Implementation of the functions from the CommandsFromWindow interface.
	 * See methods description in the interface definition.
	 */

	public void sendText(String roomName, String message) {

		System.err.println("TODO: sendText is not implemented.");

		/*
		 * TODO implement the method to send the message to the server.
		 */
	}

	public Vector<String> getChatRoomsList() {
        try {
            return server.getRoomsList();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public boolean joinChatRoom(String roomName) {
		
		System.err.println("TODO: joinChatRoom is not implemented.");

		/*
		 * TODO implement the method to join a chat room and receive notifications of new messages.
		 */		
		
		return false;		
	}
	
	public boolean leaveChatRoom(String roomName) {
		
		System.err.println("TODO: leaveChatRoom is not implemented.");

		/*
		 * TODO implement the method to leave a chat room and stop receiving notifications of new messages.
		 */		
		
		return false;
	}
		
	public boolean createNewRoom(String roomName) {
		
		System.err.println("TODO: createNewRoom is not implemented.");

		/*
		 * TODO implement the method to ask the server to create a new room (second part of the assignment only).
		 */		
		
		return false;
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */
	
	

	public void receiveMsg(String roomName, String message) {
		
		System.err.println("TODO: getName is not implemented.");
		/*
		 * TODO implement the method to allow server to publish message for client.
		 */
	}
		
	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
}