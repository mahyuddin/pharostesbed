package pharoslabut.behavior.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import pharoslabut.behavior.management.WorldModel;

public class Server extends Thread {
	WorldModel _wm;
	int _port;
	Socket[] _sockets;
	ServerSocket _serverSocket;
	PrintWriter[] _out;
	int _teamSize;

	public Server(WorldModel wm) {
		_wm = wm;
		_teamSize = _wm.getTeamSize();
		_port = _wm.getMyPort();
		_sockets = new Socket[_teamSize];
		_out = new PrintWriter[_teamSize];
	}

	public void run() {
		int i;
		try {
			_serverSocket = new ServerSocket(_port);
			for (i = 0; i < _teamSize; i++) {
				if (i != _wm.getMyIndex()) {
					_sockets[i] = _serverSocket.accept();
					_out[i] = new PrintWriter(_sockets[i].getOutputStream(),
							true);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_wm.setAllMyClientsConnected();
/*		
		while(true)
		{
			sendBehaviorToClients();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	*/	
	}

	// The robot send all it's team member its current running behavior
	public void sendBehaviorToClients() {
		int i;
		for (i = 0; i < _teamSize; i++)
			if (i != _wm.getMyIndex())
			{
				_out[i].println(_wm.getCurrentBehaviorName());
				System.out.println("Send: "+_wm.getCurrentBehaviorName()+ " Sent to Client id: "+i);
			}
	}

}
