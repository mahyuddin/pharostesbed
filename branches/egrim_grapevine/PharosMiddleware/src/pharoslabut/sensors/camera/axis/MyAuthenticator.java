package pharoslabut.sensors.camera.axis;


import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Used to handle HTTP authentication.
 *
 */
public class MyAuthenticator extends Authenticator {

	private String username; // Username needed for authentication.
	private String password; // Password needed for authentication.

	/**
	 * Create a new MyAuthenticator object.
	 * 
	 * @param username
	 * @param password
	 */
	public MyAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * This method is called when a password protected URL is accessed. The
	 * username and password is then automatically provided using the HTTP
	 * protocol.
	 */
	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username, this.password
				.toCharArray());
	}

	/**
	 * Set the username.
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the password.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

} // end public class MyAuthenticator