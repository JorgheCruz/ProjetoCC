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
 A classe Monitor está dividida em duas Threads, uma para enviar a mensagem multicast, de 10 em 10 segundos(MonitorSend) 
 e outra a receber as respostas dos agentes à mensagem de probing (MonitorReceive). 
 A Monitor-Receive, ao receber a mensagem, faz o parsing da mensagem e atualiza a tabela de Estado,
 com os parâmetros recebidos. Além disso, o Monitor, que lança estas duas threads, terá um TimerTask que é responsável 
 por acada 10 segundos, executar a tarefa de incrementar o valor deTimeoutde cada agente presenta naTabela deEstado.
 * @author grupo 28
 */

class MonitorSend extends Thread {

    /** Socket que recebe e envia DatagramPakcets*/
    private final DatagramSocket socket;

    /** Packet que é recebido e envido pelo DatagramSocket*/
    private DatagramPacket packet;

    /** Endereço ip */
    private InetAddress IP;
    /**Porta*/
    private final int port = 8888;
    /**Tempo em que enviou o ultimo pacote */
    private SharedTimeSent time;
    /**Tabela de Estados*/
    private TabelaEstado stateTable;

    /** Cifra que codifica as e verifica mensagens*/
    private Cypher cypher = new Cypher();

    /** Construtor da classe*/
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
    /**Override do método run*/
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

    /** Socket que recebe e envia DatagramPakcets*/
    private final DatagramSocket socket;
    /** Packet que é recebido pelo DatagramSocket*/
    private DatagramPacket answer;
    /**Tempo em que recebeu o ultimo packet*/
    private SharedTimeSent time;
    /**Tabela de Estado*/
    private TabelaEstado stateTable;
    /** Cifra que codifica as e verifica mensagens*/
    private Cypher cypher = new Cypher();

    /**Construtor da classe*/
    MonitorReceive(SharedTimeSent time, TabelaEstado stateTable, DatagramSocket socket) {

        this.socket = socket;
        this.time = time;
        this.stateTable = stateTable;

        byte[] aReceber = new byte[1024];
        answer = new DatagramPacket(aReceber, aReceber.length);
    }

    /**Método que atualiza a tabela de estados*/
    public void updateTable(String msg, long timeReceived, long timeSent) {
   	
	String[] info = msg.split("/");
	
	String ID = info[0];
	double ram = Double.parseDouble(info[1]);
	double cpu = Double.parseDouble(info[2]);
	long delayTime = Long.parseLong(info[3]);

	long rtt = timeReceived - timeSent - delayTime;

	this.stateTable.updateState(ID, ram, cpu, rtt);
    }

    /**Overide do metodo run*/
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

                /** Timestamp de quando recebeu a resposta */
                timeReceived = System.currentTimeMillis();

                /** Timestamp de quando enviou o pedido*/
                timeSent = this.time.getTimestamp();
                
                msgReceived = cypher.checkMessage(new String(answer.getData(), 0, answer.getLength()));

		if (msgReceived != null)
			updateTable(msgReceived, timeReceived, timeSent);
	}
    }
}

public class Monitor {
    
    /**Socket que recebe e envia pacotes*/
    private final DatagramSocket socket;
    /**Tempo desde a ultimo pacote enviado ou recebido*/
    private final SharedTimeSent time;
    /**Tabela de Estados*/
    private final TabelaEstado stateTable;
    
    /**Construtor da classe*/
    public Monitor(TabelaEstado stateTable) throws SocketException {
        this.socket = new DatagramSocket();
        this.time = new SharedTimeSent();
        this.stateTable = stateTable;
    }

    /**
     Metodo invocado pelo reverse proxy que cria as threads MonitorSend e MonitorReceive
     e inicializa tarefa de incrementação dos timeouts
     */
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


/** Classes auxiliares para o tempo*/
class SharedTimeSent {

        public volatile long timestamp;

        public void setTimestamp(long ts) { timestamp = ts; }
        public long getTimestamp() { return timestamp; }
}

/** Classes auxiliares para incrementar timeouts*/
class MyTimerTask extends TimerTask {
    
    private TabelaEstado stateTable;
    
    /**Construtor da classe*/
    MyTimerTask(TabelaEstado stateTable) {
        this.stateTable = stateTable;
    } 
    /**Override da função run*/
    public void run() {
        stateTable.incTimeout();
    }
}
