import java.io.IOException;
import java.net.SocketException;

/**
 *
 * @author treishy
 */

public class ReverseProxy {
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {

        Monitor monitor = new Monitor();
        monitor.start("239.8.8.8");

	Server
    }
}
