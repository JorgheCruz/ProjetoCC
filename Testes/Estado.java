/**
 *
 * @author treishy
 */

public class Estado {

    private double ram;
    private double cpu;
    private long rtt;
    private int bw;
    private boolean available;
    private int timeout = 0;
    private long bytesSent = 0;
    private int usedCount = 0;
    
    public Estado(){
        this.bw = 0;
        available = true;
    }
    
    public Estado(double ram, double cpu, long rtt){
        this.ram = ram;
        this.cpu = cpu;
        this.rtt = rtt;
        this.bw = 0;
	this.available = true;
    }
    public double getCpu() {
        return cpu;
    }

    public int getBw() {
        return bw;
    }

    public double getRam() {
        return ram;
    }

    public long getRtt() {
        return rtt;
    }
    
    public long getBytesSent(){ 
        return bytesSent;}
    
    public int getTimeout(){
        return timeout;
    }
    public int getUsed() {
        return usedCount;
    }
    
    public void increaseTimeout(){
        timeout++;
    }
    
    public void increaseUsed(){
        usedCount++;
    }
    
    public void resetTimeout(){
        timeout=0;
    }
    
    public void resetBytes(){
        bytesSent = 0;
    }
    
    public void resetUsed(){
        usedCount = 0;
    }
    
    public void addBytes(long bytes) {
        bytesSent+= bytes;
    }
    
    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public void setBw(int bw) {
        this.bw = bw;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public void setRtt(long rtt) {
        this.rtt = rtt;
    }
    
    public void setAvailable(){
        available=true;
    }
    
    public void setUnavailable(){
        available=false;
    }
    
    public boolean isAvailable(){return available;}

    public String toString(){
        String resposta = ("Ram = " + this.getRam() + "\nCPU Usage: " + this.getCpu() + "\nRTT: " +this.getRtt());
        return resposta;
    } 
}

