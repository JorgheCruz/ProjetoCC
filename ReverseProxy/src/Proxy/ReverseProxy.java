package Proxy;

import java.io.IOException;
import java.net.SocketException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author treishy
 */
public class ReverseProxy {
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        Monitor monitor = new Monitor();
        monitor.start("239.8.8.8");
    }
}