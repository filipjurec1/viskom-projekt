
public class Commands {
 	ProcessBuilder builder1 = new ProcessBuilder(
            "cmd.exe", "/c", "cd \"C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin>\" && ffmpeg -re -f lavfi -i aevalsrc=\"sin(400*2*PI*t)\" -ar 8000 -f mulaw -f rtp rtp://127.0.0.1:1234");

 	ProcessBuilder builder2 = new ProcessBuilder(
            "cmd.exe", "/c", "cd \"C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin>\" && ffmpeg -re -i video.mp4 -an -c:v copy -f rtp -sdp_file video.sdp -f rtp rtp://127.0.0.1:1234");

 	ProcessBuilder builder3 = new ProcessBuilder(
            "cmd.exe", "/c", "cd \"C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin>\" && ffplay rtp://127.0.0.1:1234");

 	ProcessBuilder builder4 = new ProcessBuilder(
            "cmd.exe", "/c", "cd \"C:\\Users\\Korisnik\\Desktop\\ffmpeg-20200522-38490cb-win64-static\\bin>\" && ffmpeg -f dshow -s 640x480 -r 15 -i video=\"Logitech HD Pro Webcam C920\" -f dshow -i audio=\"Microphone (HD Pro Webcam C920)\" -pix_fmt yuv420p -vsync 1 -threads 0 -vcodec libx264 -r 15 -g 30 -sc_threshold 0 -b:v 640k -bufsize 768k -maxrate 800k -preset veryfast -profile:v baseline -tune film -an -f rtp rtp://127.0.0.1:10000 -acodec aac -b:a 128k -ac 2 -ar 48000 -af \"aresample=async=1:min_hard_comp=0.100000:first_pts=0\" -vn -f rtp rtp://127.0.0.1:10002 > 10000.sdp");

}
