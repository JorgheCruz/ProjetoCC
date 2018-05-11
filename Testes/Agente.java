import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import static java.sql.DriverManager.println;
import java.net.InetAddress;
import java.net.MulticastSocket;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.math.RoundingMode;

/**
 *
 * @author treishy
 */
public class Agente {
   
	private double ram;
        private double cpu;
	private final double totalMemory;

    public Agente () {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	this.totalMemory = (double) (os.getTotalPhysicalMemorySize() / (1024*1024));
	System.out.println("Total Memory: " + this.totalMemory);
    }	

        
    public void updateEstado(){
        
	int MB = 1024*1024;
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
       
        int cores = os.getAvailableProcessors();
	double loadAvg = (double) os.getSystemLoadAverage();	
	cpu = (loadAvg / cores) * 100;
	cpu = Math.round(cpu*100)/100.0d;

        double usedMemory = (double) ((this.totalMemory - (os.getFreePhysicalMemorySize() / MB)));
	System.out.println("Used Memory: " + usedMemory);
        ram = (usedMemory / this.totalMemory) * 100;
        ram = Math.round(ram*100)/100.0d;
    }
    public String toString(){
	
	String resposta = (this.ram + "\n" + this.cpu + "\n");
        return resposta;
    }

    public static void main (String args[]) throws SocketException, IOException, InterruptedException {
        
	Agente agente = new Agente();

        MulticastSocket socketToReceive = new MulticastSocket(8888);
	InetAddress group = InetAddress.getByName("239.8.8.8"), monitorAddress;
	socketToReceive.joinGroup(group);
	DatagramSocket socketToSend = new DatagramSocket();
       
	boolean moreRequest = true;

       	while (moreRequest) {

		byte[] aReceber = new byte[1024];
		DatagramPacket request = new DatagramPacket(aReceber, aReceber.length);
        	socketToReceive.receive(request);
		String msgReceived = new String(request.getData(), 0, request.getLength()); 

		/** Timestamp when the request was send */
		//long timeSent = Long.valueOf(msgReceived);
	
		/** Delay answer */
		sleep((long) (Math.random() * 10));

		agente.updateEstado();
		String msg = agente.toString(); // + timeSent;
		monitorAddress = request.getAddress();
		DatagramPacket answer = new DatagramPacket(msg.getBytes(), msg.length(), monitorAddress, request.getPort());
		socketToSend.send(answer);
	}
        
    }
}
