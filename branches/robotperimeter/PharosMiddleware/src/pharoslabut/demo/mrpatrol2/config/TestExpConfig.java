package pharoslabut.demo.mrpatrol2.config;

public class TestExpConfig {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: TestExpConfig [config file name]");
			System.exit(1);
		}
		
		ExpConfig expConfig = new ExpConfig(args[0]);
		
		System.out.println("Contents of configuration file \"" + args[0] + "\":\n" + expConfig.toString());
	}
}
