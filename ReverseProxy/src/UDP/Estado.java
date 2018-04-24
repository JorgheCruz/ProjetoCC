/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

/**
 *
 * @author treishy
 */
public class Estado {
    private float ram;
    private float cpu;
    private float rtt;
    private float lb;
    private boolean available;

    public float getCpu() {
        return cpu;
    }

    public float getLb() {
        return lb;
    }

    public float getRam() {
        return ram;
    }

    public float getRtt() {
        return rtt;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public void setLb(float lb) {
        this.lb = lb;
    }

    public void setRam(float ram) {
        this.ram = ram;
    }

    public void setRtt(float rtt) {
        this.rtt = rtt;
    }

    /**@Override
    public String toString() {
        return super.toString();
    }
    */
   
}
