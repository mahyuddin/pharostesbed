package pharoslabut.behavior.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import pharoslabut.behavior.management.*;

public class Client extends Thread {
	private WorldModel _wm;
	Socket[] _sockets;
	int _myIndex;
	int _teamSize;

	public Client(WorldModel wm) {
		_wm = wm;
		_myIndex = _wm.getMyIndex();
		_teamSize = _wm.getTeamSize();
		_sockets = new Socket[_teamSize];

	}

	public void run() {
		int i;
/*		boolean try_again=true;*/
		for (i = 0; i < _teamSize; i++)
			if (i != _myIndex)
				try {
					_sockets[i] = new Socket(_wm.getIp(i), _wm.getPort(i));
					_sockets[i].setSoTimeout(5000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					try {
						sleep(100);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					/*Having just i-- means that we'll continue waiting for all the teammates. 
					 * Since we do not necessarily want to wait for all, we'll try twice to connect, and if it doesn't work -
					 * we'll just go on. Later, when getting the keep-alive message from a "silent" team member, we'll add it to the list. 
					
					if(try_again){
						try_again = false;
						i--;
					} else
						try_again = true; */
					i--;
					
				}
		_wm.setConnectToAllServers();
		Scanner[] teamData = new Scanner[_teamSize];
		for (i = 0; i < _teamSize; i++)
			if (i != _myIndex)
				try {
					teamData[i] = new Scanner(_sockets[i].getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		while (true) {
			for (i = 0; i < _teamSize; i++)
				if (i != _myIndex) {
					if (teamData[i].hasNextLine()) {
						String next = teamData[i].nextLine();
						System.out.println("Receive: " + next+ " from "+i);
						_wm.setTeamCurrentBehavior(next, i);	
					}
				}
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
