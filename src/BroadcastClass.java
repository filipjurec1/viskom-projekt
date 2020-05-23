import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BroadcastClass {

    private static final String LOCATION_OF_FFMPEG_BIN_TOMISLAV = "C:\\Users\\Tomi\\Downloads\\ffmpeg-20200515-b18fd2b-win64-static\\bin";
    private static final String LOCATION_OF_FFMPEG_BIN_FILIP = "C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin";
    private static final String IP_TOMISLAV = "25.88.153.87";
    private static final String IP_FILIP = "25.90.15.98";

    private static String serverIP = "";
    private static String clientIP = "";
    private static Integer serverPort = 4445;
    private static String ffmpegBinLocation = "";

    public static void main(String[] args) {

        System.out.println("Please choose role:\n1 - server\n2 - client");
        System.out.printf("Choice: ");
        Scanner in = new Scanner(System.in);
        while (true) {
            String input = in.nextLine();
            if (input.equals("1")) {
                setServerDataBasedOnUser(in);
                startServer();
            } else if (input.equals("2")) {
                setClientDataBasedOnUser(in);
                startClient();
            } else {
                System.out.println("Input must be a number, either 1 or 2!");
                continue;
            }
        }
    }

    static void setServerDataBasedOnUser(Scanner in) {
        String user = retrieveUser(in);
        if (user.equals("1")) {
            serverIP = IP_TOMISLAV;
            clientIP = IP_FILIP;
            ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
        } else {
            serverIP = IP_FILIP;
            clientIP = IP_TOMISLAV;
            ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
        }
    }

    static void setClientDataBasedOnUser(Scanner in) {
        String user = retrieveUser(in);
        if (user.equals("1")) {
            serverIP = IP_FILIP;
            clientIP = IP_TOMISLAV;
            ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
        } else {
            serverIP = IP_TOMISLAV;
            clientIP = IP_FILIP;
            ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
        }
    }

    static String retrieveUser(Scanner in) {
        System.out.printf("Please select user:\n1 - Tomislav\n2 - Filip");
        System.out.printf("Choice: ");
        return in.nextLine();
    }

    static void setServerIP(Scanner in) {
        System.out.printf("Please enter the server endpoint: ");
        serverIP = in.nextLine();
    }

    static void setServerPort(Scanner in) {
        System.out.printf("Please enter the port number of the server: ");
        serverPort = Integer.parseInt(in.nextLine());
    }

    static void startClient() {

        try ( // When you start the client program, the server should already be running and listening to the port, waiting for a client to request a connection
              Socket kkSocket = new Socket(serverIP, serverPort);
              //TODO start ffmpeg play and wait for data to come

              PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
              BufferedReader in = new BufferedReader(
                      new InputStreamReader(kkSocket.getInputStream()));
        ) {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverIP);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    serverIP);
            System.exit(1);
        }
    }

    static void startServer() {

        try (
                ServerSocket serverSocket = new ServerSocket(serverPort);     // if it cannot use te specified port because it's already used, it throws an error
                Socket clientSocket = serverSocket.accept();                // waits until a client starts up and requests a connection on the IP and port of this server
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            cmd();
            String inputLine, outputLine;

            // Initiate conversation with client
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye.")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + serverPort + " or listening for a connection.");
            System.out.println(e.getMessage());
        }

    }

    private static void cmd() {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd " + ffmpegBinLocation + " && ffmpeg -re -f lavfi -i aevalsrc=\"sin(400*2*PI*t)\" -ar 8000 -f mulaw -f rtp rtp://" + clientIP);

        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
//                System.out.println(line);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToServer() {
        //Try connect to the server on an unused port eg 9991. A successful connection will return a socket
        try (ServerSocket serverSocket = new ServerSocket(9991)) {
            Socket connectionSocket = serverSocket.accept();

            //Create Input&Outputstreams for the connection
            InputStream inputToServer = connectionSocket.getInputStream();
            OutputStream outputFromServer = connectionSocket.getOutputStream();

            Scanner scanner = new Scanner(inputToServer, "UTF-8");
            PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);

            serverPrintOut.println("Hello World! Enter Peace to exit.");

            //Have the server take input from the client and echo it back
            //This should be placed in a loop that listens for a terminator text e.g. bye
            boolean done = false;

            while (!done && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                serverPrintOut.println("Echo from <Your Name Here> Server: " + line);

                if (line.toLowerCase().trim().equals("peace")) {
                    done = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}