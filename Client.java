import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    static ClientMessageHandler cmh = null;
    static Thread cmhThread = null;

    public static void main(String args[]) {

        // Read in config file
        parseConfig(args[0]);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        try {
            server = new Socket(address, port);
            server = Handshake.clientShake(username, server);
        } catch (IOException e) {
            System.out.println("Could not connect to server");
            System.exit(1);
        }

        cmh = new ClientMessageHandler(server);
        cmhThread = new Thread(cmh);
        cmhThread.start();

        sendMessage("GTFI", username, server);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                sendMessage("GTFO",username, server);
            }
        });

        // start ui stuff
        while(true){
            try {
                displayPrompt();
                input = br.readLine();
                if(input.matches("/.*")){
                    processCommand(input);
                }else {
                    sendMessage("CHAT", input, server);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Something went went.");
            }
        }

    }

    public static void processCommand(String command){
        if(command.toLowerCase().matches("/list")){
            sendMessage("LIST",username,server);
        }
        if(command.toLowerCase().matches("/quit")){
            System.exit(0);
        }
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
    public static void displayPrompt(){
        System.out.printf("\n~> ");
    }
    private static void displayError(String s){
        System.err.println(s);
    }

    public static void sendMessage(String type, String payload, Socket client){
        String message = type + "@" + payload;
        //send message using socket
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            pw.println(message);
        }catch(IOException e){
            if(!type.matches("GTFO")) {
                System.out.println("Trouble talking to server. Shutting down client.");
                System.exit(1);
            }
        }
    }

}
