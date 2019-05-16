package main;

import java.io.*;
import java.util.List;

import crypto.analysis.CryptoScanner;
import crypto.reporting.CommandLineReporter;
import crypto.rules.CryptSLRule;
import org.apache.tools.ant.taskdefs.Exec;

public class CogniCryptAndroid implements IExecutor {

	public static void main(String...args) {
		CogniCryptAndroid app = new CogniCryptAndroid();

		ExecutorData data = new ExecutorData(args[0], args[1], args[2]);

		app.run(data);
	}

	public void run(ExecutorData data)  {
		try {
			AnalysisHelper.initializeInfoFlow(data.getAppDir(), data.getPlatform());
		} catch (Exception e) {
			AnalysisHelper.reportCallGraphError("CallGraphExceptions.txt", "CallGaph crashed on %s" , data.getAppDir());
			return;
		}

		System.out.println("Analyzing " + data.getAppDir());

		try {
			runCryptoAnalysis(data.getAppDir(), data.getRulesDir());
		} catch (Exception e) {
			AnalysisHelper.reportCallGraphError("CryptoAnalysisExceptions.txt", "CryptoAnalysis crashed on %s", data.getAppDir());
		}
	}


	private static void runCryptoAnalysis(String apkFile, String rules) {
		List<CryptSLRule> cslRules = AnalysisHelper.getRules(rules);
		File report = createReport(apkFile);

		CryptoScanner scanner = AnalysisHelper.createCryptoScanner();
		scanner.getAnalysisListener().addReportListener(new CommandLineReporter(report.getAbsolutePath(), cslRules));
		scanner.scan(cslRules);
	}

	private static File createReport(String file) {
		File report = new File("cognicrypt-reports/" + file.replace(".apk", "/"));
		report.mkdirs();
		return report;
	}
}
