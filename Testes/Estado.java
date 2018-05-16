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

    public float getBw() {
        return bw;
    }

    public double getRam() {
        return ram;
    }

    public long getRtt() {
        return rtt;
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

    public String toString(){
        String resposta = ("Ram = " + this.getRam() + "\nCPU Usage: " + this.getCpu() + "\nRTT: " +this.getRtt());
        return resposta;
    } 
}

