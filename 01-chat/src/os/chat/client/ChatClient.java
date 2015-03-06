package os.chat.client;


import os.chat.server.ChatServer;
import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManager;
import os.chat.server.ChatServerManagerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, ChatServerInterface> rooms;
    private CommandsFromServer stub;

    /**
	 * Constructor for the ChatClient. Must perform the connection to the server. If the connection is not successful, it must exit with an error.
	 * @param window
	 */
	public ChatClient(CommandsToWindow window, String userName) {
		this.window = window;
		this.userName = userName;
        this.rooms = new HashMap<>();

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
        try {
            getRoom(roomName).publish(message, userName);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
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

        Vector<String> chatRoomsList = getChatRoomsList();
        if(chatRoomsList == null || !chatRoomsList.contains(roomName)) {
            createNewRoom(roomName);
        }

        ChatServerInterface currentRoom = null;
        try {
            currentRoom = getRoom(roomName);
            currentRoom.register(getStub());
            return true;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public boolean leaveChatRoom(String roomName) {
        try {
            getRoom(roomName).unregister(getStub());
            return true;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private CommandsFromServer getStub() throws RemoteException {
        if(stub == null) {
            stub = (CommandsFromServer) UnicastRemoteObject.exportObject(this, 0);
        }
        return stub;

    }

    public boolean createNewRoom(String roomName) {
        try {
            server.createRoom(roomName);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */
	
	

	public void receiveMsg(String roomName, String message) {
		window.publish(roomName, message);
	}
		
	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.


    private ChatServerInterface getRoom(String roomName) throws RemoteException, NotBoundException {
        if(rooms.containsKey(roomName)) {
            return rooms.get(roomName);
        }
        else {
            ChatServerInterface room = (ChatServerInterface) registry.lookup(ChatServer.CHAT_SERVER_RMI_REG_PREFIX + roomName);
            rooms.put(roomName, room);
            return room;
        }
    }
}
