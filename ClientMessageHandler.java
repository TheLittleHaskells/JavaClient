import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by cg on 2/25/15.
 */
public class ClientMessageHandler implements Runnable{
    public static final int UPDATETIME = 250; // Time between checking socket list for new messages in ms
    Socket socket;
    Boolean running;

    public ClientMessageHandler(Socket s){
        this.socket = s;
        this.running = false;
    }

    /**
     * Iterates through the message list looking for new messages from connected clients, then passes
     * any messages that it finds to readMessage to be handled.
     *
     * TODO: Maybe disconnect users when IOException is caught?
     */
    @Override
    public void run() {
        this.running = true;
        while(running){
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    String input = null;
                    if((input = br.readLine()) != null){
                        readMessage(input);
                    }
                } catch (IOException e) {
                    displayError("Trouble talking to server");
                    e.printStackTrace();
                }

            try {
                Thread.sleep(UPDATETIME);
            } catch (InterruptedException e) {
                // an InterruptedException was thrown (which we don't really care about)
            }
        }
    }

    /**
     * Stop the main loop from running.
     */
    public void stop(){
        this.running = false;
    }

    /**
     * readMessage takes in a username and a message and handles the message
     * appropriately (sending to UI, relaying to other clients, etc...)
     *
     * @param msg message in string format
     */
    public static void readMessage( String msg){
        //System.out.println("msg from cmh: "+msg);
        String[] tokens = msg.split("@");
        String payload = msg.substring(5);
        //System.out.println("Tokens[0]: "+tokens[0]);
        // List users
        if(tokens[0].matches("LIST")){
            updateUserList(payload);

            // Login messages
        }else if(tokens[0].matches("GTFI")){
            displayUserLoggedIn(payload);

            // Logoff messages
        }else if(tokens[0].matches("GTFO")){
            displayUserLoggedOff(payload);

            // Chat messages
        }else if(tokens[0].matches("CHAT")) {
            displayChatMessage( payload);

            // Userlist request messages
        }else {
            displayError("Invalid message received: " + msg);
        }
        displayPrompt();
    }
    public static void updateUserList(String list){
        String [] userlist = list.split("@");
        for(int i=0; i<userlist.length; i++){
            System.out.println("\t\t\t"+userlist[i]);
        }
    }

    /**
     * Displays a message saying a user has logged on.
     *
     * @param username username of person who logged in
     */
    public static void displayUserLoggedIn(String username){
        System.out.printf("\n\t%s has logged on.", username);
    }

    /**
     * Displays a message saying a user has logged off.
     *
     * @param username username of person who logged off
     */
    public static void displayUserLoggedOff(String username){
        System.out.printf("\n\t%s has logged off.", username);
    }

    /**
     * Displays a message from a user to the screen
     *
     * @param message message
     */
    public static void displayChatMessage( String message){
        System.out.printf("\t%s", message);
    }

    /**
     * displayPrompt shows a newline and a prompt to the user.
     */
    public static void displayPrompt(){
        System.out.printf("\n~> ");
    }
    private static void displayError(String s){
        System.err.println(s);
    }


}
