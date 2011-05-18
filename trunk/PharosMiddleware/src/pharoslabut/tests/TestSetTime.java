package pharoslabut.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.exceptions.PharosException;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageSender;

public class TestSetTime {

	public static void main(String[] args) {
		TCPMessageSender sender = new TCPMessageSender();
		
		InetAddress addr;
		try {
			addr = InetAddress.getByName("192.168.1.105");
			
			SetTimeMsg msg = new SetTimeMsg();
			sender.sendMessage(addr, 7776, msg);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
