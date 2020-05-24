import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BroadcastClass {

    private static String serverIP = "";
    private static String clientIP = "";
    private static Integer serverPort = 4445;
    private static Integer clientPort = 63353;

    private static String ffmpegBinLocation = "";

    private static final String LOCATION_OF_FFMPEG_BIN_TOMISLAV = "C:\\Users\\Tomi\\Downloads\\ffmpeg-20200515-b18fd2b-win64-static\\bin";
    private static final String LOCATION_OF_FFMPEG_BIN_FILIP = "C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin";
    private static final String IP_TOMISLAV = "25.88.153.87";
    private static final String IP_FILIP = "25.90.15.98";

    //    private static String openFfmpegFolder = "cd " + ffmpegBinLocation;
    private static String ffmpegCommandStreamSineSignal =
            "ffmpeg -re -f lavfi -i aevalsrc=\"sin(400*2*PI*t)\" -ar 8000 -f mulaw -f rtp rtp://" + IP_FILIP + ":" + clientPort + " -sdp_file audio.sdp";
    private static String ffmpegCommandStreamWebcamVideo = "ffmpeg -f dshow -i video=\"HD WebCam\" rtp://" + serverIP;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        setDataAndStartProgram(in);
    }

    static void setDataAndStartProgram(Scanner in) {
        String user = retrieveUser(in);
        switch (user) {
            case "1":
                serverIP = IP_FILIP;
                clientIP = IP_TOMISLAV;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
                startServer();
                break;
            case "2":
                serverIP = IP_TOMISLAV;
                clientIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
                startClient();
                break;
            case "3":
                serverIP = IP_TOMISLAV;
                clientIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
                startServer();
                break;
            case "4":
                serverIP = IP_FILIP;
                clientIP = IP_TOMISLAV;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
                startClient();
                break;
            case "5":
                startServer();
                break;
            case "6":
                setServerIP(in);
                startClient();
                break;
            default:
                System.out.println("Illegal value was entered. Program will end.");
                System.exit(0);
                break;
        }
    }

    static String retrieveUser(Scanner in) {
        System.out.println();
        System.out.println("1 - Filip is the server");
        System.out.println("2 - Filip is the client");
        System.out.println("3 - Tomislav is the server");
        System.out.println("4 - Tomislav is the client");
        System.out.println("5 - Custom user is server");
        System.out.println("6 - Custom user is client");
        System.out.printf("Choice: ");
        return in.nextLine();
    }

    static void setServerIP(Scanner in) {
        System.out.printf("Please enter the server endpoint: ");
        serverIP = in.nextLine();
    }

    static void startClient() {
        System.out.println("\nClient is starting...");

        try (
                // When you start the client program, the server should already be running and listening to the port, waiting for a client to request a connection
                Socket socket = new Socket(serverIP, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            System.out.println("* Connected to server!");
            clientPort = socket.getLocalPort();
            System.out.println("* Your port is " + clientPort);

            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            //TODO start ffmpeg play and wait for data to come
            cmd("client");

            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
            }

//                String fromUser;
//                fromUser = stdIn.readLine();
//                if (fromUser != null) {
//                    System.out.println("Client: " + fromUser);
//                    out.println(fromUser);
//                }
//            }
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
        System.out.println("\nServer is starting...");

        try (
                ServerSocket serverSocket = new ServerSocket(serverPort);     // if it cannot use te specified port because it's already used, it throws an error
                Socket clientSocket = serverSocket.accept();                // waits until a client starts up and requests a connection on the IP and port of this server
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("* Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " connected!");
            clientPort = clientSocket.getPort();

            cmd("server");

//            String inputLine, outputLine;
//            //  Initiate conversation with client
//            KnockKnockProtocol kkp = new KnockKnockProtocol();
//            outputLine = kkp.processInput(null);
//            out.println(outputLine);
//
//            while ((inputLine = in.readLine()) != null) {
//                outputLine = kkp.processInput(inputLine);
//                out.println(outputLine);
//                if (outputLine.equals("Bye.")) {
//                    break;
//                }
//            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + serverPort + " or listening for a connection.");
            System.out.println(e.getMessage());
        }

    }

    private static void cmd(String param) {
        ProcessBuilder builder = null;

        if (param.equals("server")) {
            builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd " + LOCATION_OF_FFMPEG_BIN_TOMISLAV + " && " + ffmpegCommandStreamSineSignal);
        } else if (param.equals("client")) {
            builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd " + LOCATION_OF_FFMPEG_BIN_FILIP + " && ffplay rtp://" + clientIP + ":" + clientPort);
        } else return;

        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
            System.out.println("* cmd command executed!");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}