import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


/**
 * The Server entry point.
 */
public class Client {
    static int port;   // port
    static String[] userlist;
    static String username;
    static InetAddress address;
    static Socket server = null;


    public static void main(String args[]){
        // Read in config file
        parseConfig(args[0]);


        // start ui stuff
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        try {
            server = new Socket(address,port);
            server = Handshake.clientShake(username,server);
        } catch (IOException e) {
            System.out.println("Could not connect to server");
            System.exit(1);
        }

        while(true){
            try {
                displayPrompt();
                input = br.readLine();
                if(input.matches("/.*")){
                   // processCommand();

                }else {
                    sendMessage("CHAT",input,server);
                    // send to all clients here.
                    /*for(Map.Entry<String, Socket> entry : listener.getSocketList().entrySet()){
                        Socket toSendTo = entry.getValue();
                        sendMessage("CHAT", input, toSendTo);
                        break;
                    }*/
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Something went went.");
            }
        }
    }

    public static void sendMessage(String type,String payload, Socket client){
        String message = type + "@" + payload;

        //send message using socket
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            pw.println(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void readMessage(String username, String msg){
        String[] tokens = msg.split("@");
        String payload = msg.substring(5);

        // List users
        if(tokens[0].matches("LIST")){
            updateUserList(payload);

            // Login messages
        }else if(tokens[0].matches("GTFI")){
            displayUserLoggedIn(payload);

            // Logoff messages
        }else if(tokens[0].matches("GTFO")){
            displayUserLoggedOff(payload);
            try {
            }catch (Exception e){

            }

            // Chat messages
        }else if(tokens[0].matches("CHAT")) {
            displayChatMessage(username, payload);
            // Userlist request messages
        }else {
            displayError("Invalid message received: " + msg);
        }
    }

    /**
     * updates the stored list of users
     *
     * @param list formatted userlist string (should be delimited by @)
     */
    public static void updateUserList(String list){
        userlist = list.split("@");
    }

    /**
     * Displays a message saying a user has logged on.
     *
     * @param username username of person who logged in
     */
    public static void displayUserLoggedIn(String username){
        System.out.printf("\n\t%s has logged on.", username);
        displayPrompt();
    }

    /**
     * Displays a message saying a user has logged off.
     *
     * @param username username of person who logged off
     */
    public static void displayUserLoggedOff(String username){
        System.out.printf("\n\t%s has logged off.", username);
        displayPrompt();
    }

    /**
     * Displays a message from a user to the screen
     *
     * @param username source username
     * @param message message
     */
    public static void displayChatMessage(String username, String message){
        System.out.printf("\n\t%s: %s", username, message);
        displayPrompt();
    }

    /**
     * displayPrompt shows a newline and a prompt to the user.
     */
    public static void displayPrompt(){
        System.out.printf("\n~> ");
    }

    public static String generateUserList(HashMap<String,Socket> userMap){
        String payload ="";
        for(Map.Entry<String, Socket> entry: userMap.entrySet()) {
            String user = entry.getKey();
            payload = payload + "@" + user;
        }
        return payload.substring(1);
    }


    /**
     * Reads in the config file
     *
     * Formatted like <option>=<value>
     *
     */
    public static void parseConfig(String filename){
        File f = new File(filename);
        if(!f.exists()){
            System.err.println("Config file didn't exist.");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String input = null;

            // read in a line, and if it's not null, do what's inside the loop
            while((input = br.readLine()) != null) {
                String[] tokens = input.split("=");
                if(tokens.length != 2){
                    displayError("Invalid config file..");
                    return;
                }

                String key = tokens[0];
                String value = tokens[1];

                // Grabs port to connect to from config file
                if(key.toUpperCase().matches("PORT")) {
                    port = Integer.parseInt(value);
                    continue;
                    // Grabs ip address to connect to from config file
                }else if(key.toUpperCase().matches("ADDRESS")){
                    try{
                        address = InetAddress.getByName(value);
                    } catch (UnknownHostException e){
                        e.printStackTrace();
                    }
                    continue;
                }else if(key.toUpperCase().matches("USERNAME")){
                    username = value;
                    continue;
                }else{
                    displayError("Invalid config option");
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void displayError(String s){
        System.err.println(s);
    }
}
