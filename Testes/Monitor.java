import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author treishy
 */

class MonitorSend extends Thread {

    private final DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress IP;
    private final int port = 8888;
    private SharedTimeSent time;
    private TabelaEstado stateTable;
    private Cypher cypher = new Cypher();

    MonitorSend(SharedTimeSent time, TabelaEstado stateTable, DatagramSocket socket, String address) throws IOException {

        try{
            IP = InetAddress.getByName(address);
        }
        catch(UnknownHostException e) {
            IP = null;
            System.err.println(e.getMessage());
        }

        this.time = time;
        this.socket = socket;
        this.stateTable = stateTable;
    }

    public void run() {

        int sleeptime = 10000;
        
        if (IP != null) {
		String request = "revproinfo";
		String requestHash = cypher.encrypt(request);
		String msg = request + "\n" + requestHash;
                this.packet = new DatagramPacket(msg.getBytes(), msg.length(), IP, port);
        }

        while (true) {

                this.time.setTimestamp(System.currentTimeMillis());

                try {
                        socket.send(this.packet);

                        sleep(sleeptime);
                }
                catch(IOException|InterruptedException e) {
                        System.err.println(e.getMessage());
                }

		System.out.print("\033[H\033[2J");
		System.out.print(this.stateTable.toString());
        }
    }

}

class MonitorReceive extends Thread {

    private final DatagramSocket socket;
    private DatagramPacket answer;
    private SharedTimeSent time;
    private TabelaEstado stateTable;
    private Cypher cypher = new Cypher();

    MonitorReceive(SharedTimeSent time, TabelaEstado stateTable, DatagramSocket socket) {

        this.socket = socket;
        this.time = time;
        this.stateTable = stateTable;

        byte[] aReceber = new byte[1024];
        answer = new DatagramPacket(aReceber, aReceber.length);
    }

    public void updateTable(String msg, long timeReceived, long timeSent) {
   	
	String[] info = msg.split("/");
	
	String ID = info[0];
	double ram = Double.parseDouble(info[1]);
	double cpu = Double.parseDouble(info[2]);
	long delayTime = Long.parseLong(info[3]);

	long rtt = timeReceived - timeSent - delayTime;

	this.stateTable.updateState(ID, ram, cpu, rtt);
    }

    @Override
    public void run() {
        
        String msgReceived;
        long timeReceived, timeSent;

        while (true) {

                try {
                        socket.receive(answer);
                }
                catch (IOException e) {
                        System.err.println(e.getMessage());
                }

                /** Timestamp when answer was received */
                timeReceived = System.currentTimeMillis();

                /** Timestamp when request was sent */
                timeSent = this.time.getTimestamp();
                
                msgReceived = cypher.checkMessage(new String(answer.getData(), 0, answer.getLength()));

		if (msgReceived != null)
			updateTable(msgReceived, timeReceived, timeSent);
	}
    }
}

public class Monitor {
    
    private final DatagramSocket socket;
    private final SharedTimeSent time;
    private final TabelaEstado stateTable;
    
    public Monitor(TabelaEstado stateTable) throws SocketException {
        this.socket = new DatagramSocket();
        this.time = new SharedTimeSent();
        this.stateTable = stateTable;
    }

    public void start(String address) throws IOException, InterruptedException {
        
        TimerTask timerTask = new MyTimerTask(stateTable);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 10000, 10000);

        Thread send = new MonitorSend(time, stateTable, socket, address);
        Thread receive = new MonitorReceive(time, stateTable, socket);
        send.start();
        receive.start();
    }

}

class SharedTimeSent {

        public volatile long timestamp;

        public void setTimestamp(long ts) { timestamp = ts; }
        public long getTimestamp() { return timestamp; }
}

class MyTimerTask extends TimerTask {
    
    private TabelaEstado stateTable;
    
    MyTimerTask(TabelaEstado stateTable) {
        this.stateTable = stateTable;
    } 
    
    public void run() {
        stateTable.incTimeout();
    }
}
