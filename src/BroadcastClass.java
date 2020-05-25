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
    private static Integer communicationPort = 12345;
    private static Integer rtpPort = 12346;
    private static String ffmpegBinLocation = "";

    private static final String LOCATION_OF_FFMPEG_BIN_TOMISLAV = "C:\\Users\\Tomi\\Downloads\\ffmpeg-20200515-b18fd2b-win64-static\\bin";
    private static final String LOCATION_OF_FFMPEG_BIN_FILIP = "C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin";
    private static final String IP_TOMISLAV = "25.88.153.87";
    private static final String IP_FILIP = "25.90.15.98";
    private static final String LOCALHOST = "127.0.0.1";

    private static String ffmpegCommandStreamSineSignal =
            "ffmpeg -re -f lavfi -i aevalsrc=\"sin(400*2*PI*t)\" -ar 8000 -f mulaw -f rtp rtp://" + clientIP + ":" + communicationPort;
    private static String ffplayCommandGetSound = "ffplay rtp://" + serverIP + ":" + communicationPort;
    private static String ffmpegCommandStreamWebcamVideo = "ffmpeg -f dshow -i video=\"HD WebCam\" -f rtp rtp://" + clientIP + " -sdp_file webcam_sdp";
    private static String ffplayCommandGetWebcamVideo = "ffplay -protocol_whitelist \"file,rtp,udp\" webcam_sdp";

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
                startServer(in);
                break;
            case "2":
                serverIP = IP_TOMISLAV;
                clientIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
                startClient(in);
                break;
            case "3":
                serverIP = IP_TOMISLAV;
                clientIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
                startServer(in);
                break;
            case "4":
                serverIP = IP_FILIP;
                clientIP = IP_TOMISLAV;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
                startClient(in);
                break;
            case "5":
                startServer(in);
                break;
            case "6":
                setServerIP(in);
                startClient(in);
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

    static void startClient(Scanner cmdInput) {

        System.out.println("\nClient is starting...");

        try (
                // When you start the client program, the server should already be running and listening to the port,
                // waiting for a client to request a connection
                Socket socket = new Socket(serverIP, rtpPort)
        ) {
            System.out.println("* Connected to server!\n");

            System.out.println("Choose action:");
            System.out.println("1 - Receive video stream");
            System.out.println("2 - Receive audio stream");
            System.out.println("3 - Receive video and audio streams");
            System.out.print("Choice: ");
            switch (Integer.parseInt(cmdInput.nextLine())) {
                case 1:
                    cmd(ffplayCommandGetWebcamVideo);
                    break;
                case 2:
                    cmd(ffplayCommandGetSound);
                    break;
                case 3:
                    Thread videoThread = new Thread(() -> cmd(ffplayCommandGetSound));
                    videoThread.start();
                    Thread audioThread = new Thread(() -> cmd(ffplayCommandGetWebcamVideo));
                    audioThread.start();
                    break;
                default:
                    System.err.println("Wrong value was input, exiting program.");
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverIP);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverIP);
            System.exit(1);
        }
    }

    static void startServer(Scanner cmdInput) {
        System.out.println("\nServer is starting...");

        try (
                // If it cannot use te specified port because it's already used, it throws an error
                ServerSocket serverSocket = new ServerSocket(rtpPort);
                // It waits until a client starts up and requests a connection to the IP and port of this server
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("* Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " connected!");

            System.out.println("Choose action:");
            System.out.println("1 - Send video stream");
            System.out.println("2 - Send audio stream");
            System.out.println("3 - Send video and audio streams");
            System.out.print("Choice: ");
            switch (Integer.parseInt(cmdInput.nextLine())) {
                case 1:
                    cmd(ffmpegCommandStreamWebcamVideo);
                    break;
                case 2:
                    cmd(ffmpegCommandStreamSineSignal);
                    break;
                case 3:
                    Thread videoThread = new Thread(() -> cmd(ffmpegCommandStreamWebcamVideo));
                    videoThread.start();
                    Thread audioThread = new Thread(() -> cmd(ffmpegCommandStreamSineSignal));
                    audioThread.start();
                    break;
                default:
                    System.err.println("Wrong value was input, exiting program.");
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + rtpPort + " or listening for a connection.");
            System.out.println(e.getMessage());
        }
    }

    private static void cmd(String command) {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd " + ffmpegBinLocation + " && " + command);

        builder.redirectErrorStream(true);
        Process p = null;

        try {
            p = builder.start();
            System.out.println("* Command executed!");
            System.out.println("------------------------------------------");

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