import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.UnknownHostException;

/**
 *
 * @author treishy
 */
public class Agente {
   
	private double ram;
        private double cpu;
	private long delayTime;
	private final double totalMemory;
	private final String localIP;

    public Agente() throws UnknownHostException {

	this.localIP = InetAddress.getLocalHost().getHostAddress();
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	this.totalMemory = (double) (os.getTotalPhysicalMemorySize() / (1024*1024));
    }	
        
    public void updateEstado() {
        
	int MB = 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
       
	/** Calculate free CPU */
        int cores = os.getAvailableProcessors();
	double loadAvg = (double) os.getSystemLoadAverage();	
	cpu = (loadAvg / cores) * 100;
	cpu = 100 - (Math.round(cpu * 100) / 100.0d);

	/** Calculate free RAM */
        double usedMemory = (double) ((this.totalMemory - (os.getFreePhysicalMemorySize() / MB)));
        ram = (usedMemory / this.totalMemory) * 100;
        ram = 100 - (Math.round(ram * 100) / 100.0d);
    }

    public String toString(String serverID) {

	String resposta = (serverID + "/" + this.ram + "/" + this.cpu + "/" + this.delayTime);
        return resposta;
    }

    public static void main (String args[]) throws SocketException, IOException, InterruptedException {
        
	Agente agente = new Agente();
        String serverID = agente.localIP + ":" + args[0];
	Cypher cypher = new Cypher();

	/** Join Multicast Socket in IP 239.8.8.8 and Port 8888 */
        MulticastSocket socketToReceive = new MulticastSocket(8888);
	InetAddress group = InetAddress.getByName("239.8.8.8");
	socketToReceive.joinGroup(group);

	DatagramSocket socketToSend = new DatagramSocket();
	
	byte[] received = new byte[1024];
	DatagramPacket requestPacket = new DatagramPacket(received, received.length);
	DatagramPacket answerPacket; 
       
       	while (true) {

        	socketToReceive.receive(requestPacket);
		String requestMessage = cypher.checkMessage(new String(requestPacket.getData(), 0, requestPacket.getLength()));
		
		/** Check request integrity and authentication */
		if (requestMessage != null) {

			/** Delay answer between 0 and 10 ms */
			agente.delayTime = (long) (Math.random() * 10);
			sleep(agente.delayTime);

			agente.updateEstado();
			String answer = agente.toString(serverID);
			String answerHash = cypher.encrypt(answer);
			String msg = answer + "\n" + answerHash;

			/** Prepare and send Datagram Packet with server info */
			answerPacket = new DatagramPacket(msg.getBytes(), msg.length(), requestPacket.getAddress(), requestPacket.getPort());
			socketToSend.send(answerPacket);
		}
	}
        
    }
}
