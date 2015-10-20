package edu.gatech.sjpcap;

import java.net.*;

public class IPPacket extends Packet{

    public long timestamp;
    
    public InetAddress src_ip;
    public InetAddress dst_ip;

    public IPPacket(long timestamp){
	this.timestamp = timestamp;
    }
}
