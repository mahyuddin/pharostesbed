package pharoslabut.behavior.fileParsing;

public class StringParsing {

	public static String removePrefix(String str, String prefix)
	{
		if(str.startsWith(prefix))
			return str.substring(prefix.length());
		else
			return str;
	}
	public static boolean havePrefix(String str, String prefix)
	{
		if(str.startsWith(prefix))
			return true;
		else
			return false;
	}
}
