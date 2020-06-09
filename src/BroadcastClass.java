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

    private static final String LOCATION_OF_FFMPEG_BIN_TOMISLAV = "C:\\Users\\Tomi\\Downloads\\ffmpeg-20200515-b18fd2b-win64-static\\bin";
    private static final String LOCATION_OF_FFMPEG_BIN_FILIP = "C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin";
    private static final String IP_TOMISLAV = "25.88.153.87";
    private static final String IP_FILIP = "25.90.15.98";
    private static final String LOCALHOST = "127.0.0.1";

//    private static String ffmpegRecordVideoAudio = "ffmpeg -f dshow -i video=\"HD WebCam\":audio=\"Microphone Array (Intel® Smart Sound Technology (Intel® SST))\"";

    // Sending
    private static String ffmpegStreamWebcamVideo
            = "ffmpeg -f dshow -i video=\"HD WebCam\" -vcodec mpeg4 -f mpegts -f rtp rtp://" + clientIP + ":" + rtpPort + " -sdp_file file.sdp"; //25.105.181.67:12346
    private static String ffmpegStreamWebcamVideoAndAudio
            = "ffmpeg -f dshow -i video=\"HD WebCam\":audio=\"Microphone Array (Intel® Smart Sound Technology (Intel® SST))\" -vn -f rtp rtp://" + clientIP + ":" + rtpPort
            + " -acodec libopus -an -f rtp rtp://" + clientIP + ":" + rtpPort + 1 + " -sdp_file file.sdp"; // 25.105.181.67:12346
    private static String ffmpegStreamLocalVideo =
            "ffmpeg -re -i video.mp4 -an -c:v copy -f rtp rtp://25.105.181.67:12346 -sdp_file file.sdp";
    private static String ffmpegStreamAudio =
            "ffmpeg -f dshow -i audio=\"Microphone Array (Intel® Smart Sound Technology (Intel® SST))\"" +
                    " -acodec libopus -f rtp rtp://25.105.181.67:12346 -sdp_file file.sdp"; //TODO ne radi, klijent zablokira

    //Receiving
    private static String ffplayCommand
            = "ffplay -protocol_whitelist \"file,rtp,udp\" file.sdp";

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
                Socket socket = new Socket(serverIP, serverTcpPort)
        ) {
            System.out.println("* Connected to server!\n");
            //TODO receive sdp file at this point

            System.out.println("Choose action:");
            System.out.println("1 - Receive stream");
            System.out.print("Choice: ");
            switch (Integer.parseInt(cmdInput.nextLine())) {
                case 1:
                    cmd(ffplayCommand);
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
                ServerSocket serverSocket = new ServerSocket(serverTcpPort);
                // It waits until a client starts up and requests a connection to the IP and port of this server
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            System.out.println("* Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " connected!");

            System.out.println("Choose action:");
            System.out.println("1 - Send webcam video stream");
            System.out.println("2 - Send microphone audio stream");
            System.out.println("3 - Send webcam video and audio streams");
            System.out.println("4 - Send local video stream");
            System.out.print("Choice: ");
            switch (Integer.parseInt(cmdInput.nextLine())) {
                case 1:
                    cmd(ffmpegStreamWebcamVideo);
                    break;
                case 2:
                    cmd(ffmpegStreamAudio);
                    break;
                case 3:
                    Thread videoThread = new Thread(() -> cmd(ffmpegStreamWebcamVideoAndAudio));
                    break;
                case 4:
                    cmd(ffmpegStreamLocalVideo);
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