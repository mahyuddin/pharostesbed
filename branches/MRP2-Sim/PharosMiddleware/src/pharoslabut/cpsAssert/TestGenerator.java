package pharoslabut.cpsAssert;

import java.io.*;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


public class TestGenerator {

	public static void main(String[] args) throws Exception {
		// create the source
		File sourceFile   = new File("src/pharoslabut/cps/GeneratedTests.java");
		FileWriter writer = new FileWriter(sourceFile);

		writer.write(
				"package pharoslabut.cps;\n\n" +
				"import static org.junit.Assert.*;\n" + 
				"import org.junit.Test;\n\n" + 

				"public class GeneratedTests{ \n" +
					"\t@Test\n" + 
					"\tpublic void testSimple() { \n" +
						"\t\tassertEquals(\"Simple Test1\", 2.3, 2.5, 0.5);\n" +
						"\t\tassertEquals(\"Simple Test2\", 2.1, 2.5, 0.0);\n" +
					"\t}\n\n" +
					"\t@Test\n" + 
					"\tpublic void testAnother() { \n" +
						"\t\tassertEquals(2.1, 2.5, 0.3);\n" +
					"\t}\n" +
				"}"
		);
		writer.close();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("bin/")));
		// Compile the file
		compiler.getTask(null,
				fileManager,
				null,
				null,
				null,
				fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile)))
				.call();
		fileManager.close();


		// delete the source file
		// sourceFile.deleteOnExit();

		runIt();
	}

	@SuppressWarnings("unchecked")
	public static void runIt() {
		try {
			Class params[] = {};
			Object paramsObj[] = {};
			Class thisClass = Class.forName("pharoslabut.cps.GeneratedTests");
			JUnitCore core = new JUnitCore();
			core.addListener(new TestGeneratorListener());
			
			core.run(Request.method(thisClass, "testAnother"));
//			Result res = core.run(thisClass);
			
//			for (Failure f : res.getFailures()) {
//				System.out.println(f.getMessage());
//			}
			
//			Object iClass = thisClass.newInstance();
//			Method thisMethod = thisClass.getDeclaredMethod("testSimple", params);
//			thisMethod.invoke(iClass, paramsObj);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class TestGeneratorListener extends RunListener {

	/**
	 * @see org.junit.runner.notification.RunListener#testFailure(org.junit.runner.notification.Failure)
	 */
	@Override
	public void testFailure(Failure failure) throws Exception {
		super.testFailure(failure);
		System.out.println("\nTest Failed: " + failure.getMessage());
	}

	/**
	 * @see org.junit.runner.notification.RunListener#testFinished(org.junit.runner.Description)
	 */
	@Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);
		System.out.println("\nTest finished.");
		System.out.println(description.getClassName());
		System.out.println(description.getDisplayName());
		System.out.println(description.getAnnotations());
		System.out.println(description.getMethodName());
	}

	/**
	 * @see org.junit.runner.notification.RunListener#testRunFinished(org.junit.runner.Result)
	 */
	@Override
	public void testRunFinished(Result result) throws Exception {
		super.testRunFinished(result);
		System.out.println("\n*****************************************");
		System.out.println("********** All tests finished. **********");
		System.out.println("*****************************************\n");
	}
	
}
