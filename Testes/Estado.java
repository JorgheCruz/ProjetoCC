/**
 A classe Estado carateriza um servidor num determinado momento tendo em conta vários atributos do mesmo
 @author Grupo 28
 */

public class Estado {
    /** Ram livre*/
    private double ram;
    /** CPU livre*/
    private double cpu;
    /** Round trip time entre servidor*/
    private long rtt;
    /** Estimativa da largura de banda*/
    private int bw;
    /** Disponibilidade*/
    private boolean available;
    /** Numero de timeouts*/
    private int timeout = 0;
    /** Quantidade de bytes enviada*/
    private long bytesSent = 0;
    /** O numero de vezes utilizado*/
    private int usedCount = 0;
    
    /** Construtor vazio*/
    public Estado() {
        this.bw = 0;
        available = true;
    }
    
    /** 
     Contrutor
     @param ram ram disponivel
     @param cpu cpu disponivel
     @param rtt round trip time
    */
    public Estado(double ram, double cpu, long rtt){
        this.ram = ram;
        this.cpu = cpu;
        this.rtt = rtt;
        this.bw = 0;
	    this.available = true;
    }
    
    /** Getters */
    public double getCpu() { return cpu; }
    public double getRam() { return ram; }    
    public long getRtt() { return rtt; } 
    public long getBytesSent() { return bytesSent; }
    public int getTimeout() { return timeout; }
    public int getUsed() { return usedCount; }
    public int getBw() { return bw; }


    /** Incremento do timeout*/
    public void increaseTimeout() { timeout++; }
    /** Incremento de vezes utilizado*/
    public void increaseUsed() { usedCount++; }
    
    /** Funções de reset Timout, bytes enviados e numero de vezes usado*/
    public void resetTimeout() { timeout = 0; }
    public void resetBytes() { bytesSent = 0; }
    public void resetUsed() { usedCount = 0; }
    
    /** Aumenta o numero de bytes enviados*/
    public void addBytes(long bytes) { bytesSent += bytes; }

    /** Setters*/
    public void setCpu(double cpu) { this.cpu = cpu; }
    public void setBw(int bw) { this.bw = bw; }
    public void setRam(double ram) { this.ram = ram; }
    public void setRtt(long rtt) { this.rtt = rtt; }
    public void setAvailable() { available = true; }
    public void setUnavailable(){ available = false; }
    
    /** Verificar disponibilidade*/
    public boolean isAvailable(){ return available; }

    /** Override da função toString
    
    @return string correspondente ao estado
    */
    public String toString(){
        String resposta = ("Ram = " + this.getRam() + "\nCPU Usage: " + this.getCpu() + "\nRTT: " +this.getRtt());
        return resposta;
    } 
}

