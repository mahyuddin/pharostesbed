package pharoslabut.io;

/**
 * An Acked message is one that is automatically acked through the same TCP socket
 * through which the message was sent.
 * 
 * @author Chien-Liang Fok
 *
 */
public interface AckedMsg extends Message {

}
