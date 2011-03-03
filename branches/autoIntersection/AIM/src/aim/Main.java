package aim;

/**
 *
 * @author ut
 * 
 */
public class Main {

    /**
     * call the IntersectionManager and start running the code
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        Thread IM = new IntersectionManager();
        IM.start();
    }

}
