package scjlibs.safeutil;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class VectorTestRunner {

	public static void main(String[] args) {

		Result result = JUnitCore.runClasses(VectorTest.class);

		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}

	}

}