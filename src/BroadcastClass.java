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
    private static Integer serverTcpPort = 12345;
    private static Integer rtpPort = 12346;
    private static String ffmpegBinLocation = "";
    private static String webcamName = "";
    private static String microphoneName = "";
    private static boolean userIsNew = false;
    private static boolean userIsServer = false;

    private static final String LOCATION_OF_FFMPEG_BIN_TOMISLAV = "C:\\Users\\Tomi\\Downloads\\ffmpeg-20200515-b18fd2b-win64-static\\bin";
    private static final String LOCATION_OF_FFMPEG_BIN_FILIP = "C:\\Users\\Korisnik\\Desktop\\ffmpeg-202000617-0b3bd00-win64-static\\bin";
    private static final String WEBCAM_NAME_TOMISLAV = "HD WebCam";
    private static final String WEBCAM_NAME_FILIP = "HP HD Camera";
    private static final String MICROPHONE_NAME_TOMISLAV = "Microphone Array (Intel® Smart Sound Technology (Intel® SST))";
    private static final String MICROPHONE_NAME_FILIP = "Microphone (Conexant ISST Audio)";
    private static final String IP_TOMISLAV = "25.88.153.87";
    private static final String IP_FILIP = "25.90.15.98";

    private static String ffmpegRecordVideoAudio = "ffmpeg -f dshow -i video=" + webcamName + ":audio=" + microphoneName;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        setData(in);

        if (userIsServer) {
            startServer(in);
        } else {
            startClient(in);
        }
    }

    static void setData(Scanner in) {
        String user = retrieveUser(in);
        switch (user) {
            case "1":
                serverIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;
                microphoneName = MICROPHONE_NAME_FILIP;
                webcamName = WEBCAM_NAME_FILIP;

                clientIP = IP_TOMISLAV;
                userIsServer = true;
                break;
            case "2":
                clientIP = IP_FILIP;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_FILIP;

                serverIP = IP_TOMISLAV;
                break;
            case "3":
                serverIP = IP_TOMISLAV;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;
                microphoneName = MICROPHONE_NAME_TOMISLAV;
                webcamName = WEBCAM_NAME_TOMISLAV;

                clientIP = IP_FILIP;
                userIsServer = true;
                break;
            case "4":
                clientIP = IP_TOMISLAV;
                ffmpegBinLocation = LOCATION_OF_FFMPEG_BIN_TOMISLAV;

                serverIP = IP_FILIP;
                break;
            case "5":
                retrieveBinLocation(in);
                retrieveDeviceNames(in);
                userIsServer = true;
                break;
            case "6":
                retrieveBinLocation(in);
                setServerIP(in);
                break;
            default:
                System.out.println("An illegal value was entered. Program is ending.");
                System.exit(0);
                break;
        }
    }

    static String retrieveUser(Scanner in) {
        System.out.print("\nPlease choose your role:\n");
        System.out.println("1 - Filip is the server");
        System.out.println("2 - Filip is the client");
        System.out.println("3 - Tomislav is the server");
        System.out.println("4 - Tomislav is the client");
        System.out.println("5 - New user is server");
        System.out.println("6 - New user is client");
        System.out.print("Choice: ");
        return in.nextLine();
    }

    static void retrieveBinLocation(Scanner in) {
        System.out.println("Please enter the full location of your ffmpeg bin folder (e.g. C:\\Users\\User1\\Documents\\ffmpeg-2020xxyy-xxxxxxx-win64-static\\bin):");
        ffmpegBinLocation = in.nextLine();
    }

    static void retrieveDeviceNames(Scanner in) {
        System.out.print("\nPlease enter the name of your webcam, without quotation marks (use command \"ffmpeg -list_devices true -f dshow -i dummy\"): ");
        webcamName = in.nextLine();
        System.out.print("\nPlease enter the name of your microphone, without quotation marks (use command \"ffmpeg -list_devices true -f dshow -i dummy\"): ");
        microphoneName = in.nextLine();
        System.out.print("\n");
    }

    static void setServerIP(Scanner in) {
        System.out.printf("Please enter the IP address of the server: ");
        serverIP = in.nextLine();
    }

    static void startClient(Scanner cmdInput) {

        System.out.println("\nClient is starting...");

        try (
                // When you start the client program, the server should already be running and listening to the port,
                // waiting for a client to request a connection
                Socket socket = new Socket(serverIP, serverTcpPort);
        ) {
            System.out.println("* Connected to server!\n");

            System.out.print("Press enter to receive stream.");
            cmdInput.nextLine();

            String ffplayCommand =
                    "ffplay -protocol_whitelist \"file,rtp,udp\" file.sdp";
            cmd(ffplayCommand);
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
                ServerSocket serverSocket = new ServerSocket(serverTcpPort);
                // It waits until a client starts up and requests a connection to the IP and port of this server
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("* Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " connected!");

            System.out.println("Please choose an action:");
            System.out.println("1 - Send live video stream");
            System.out.println("2 - Send live audio stream");
            System.out.println("3 - Send live video and audio streams");
            System.out.println("4 - Send saved video stream");
            System.out.print("Choice: ");
            switch (Integer.parseInt(cmdInput.nextLine())) {
                case 1:
                    String ffmpegStreamLiveVideo =
                            "ffmpeg -f dshow -i video=\"" + webcamName + "\" -c:v libx264 -f mpegts -f rtp rtp://" + clientIP + ":" + rtpPort + " -sdp_file file.sdp";
                    cmd(ffmpegStreamLiveVideo);
                    break;
                case 2:
                    String ffmpegStreamLiveAudio =
                            "ffmpeg -f dshow -i audio=\"" + microphoneName + "\" -acodec libopus -f rtp rtp://" + clientIP + ":" + rtpPort + " -sdp_file file.sdp";
                    cmd(ffmpegStreamLiveAudio);
                    break;
                case 3:
                    String ffmpegStreamLiveVideoAndAudio =
                            "ffmpeg -f dshow -i video=\"" + webcamName + "\":audio=\"" + microphoneName + "\" -vn -f rtp rtp://" + clientIP + ":" + rtpPort
                                    + " -acodec libopus -an -f rtp rtp://" + clientIP + ":" + rtpPort + 1 + " -sdp_file file.sdp";
                    Thread videoThread = new Thread(() -> cmd(ffmpegStreamLiveVideoAndAudio));
                    break;
                case 4:
                    String ffmpegStreamSavedVideo =
                            "ffmpeg -re -i video.mp4 -an -c:v copy -f rtp rtp://" + clientIP + ":" + rtpPort + " -sdp_file file.sdp";
                    cmd(ffmpegStreamSavedVideo);
                    break;
                default:
                    System.err.println("Wrong value was input, exiting program.");
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + serverTcpPort + " or listening for a connection.");
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