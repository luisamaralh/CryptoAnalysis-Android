package main;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import boomerang.callgraph.ObservableDynamicICFG;
import boomerang.callgraph.ObservableICFG;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import boomerang.preanalysis.BoomerangPretransformer;
import crypto.analysis.CryptoScanner;
import crypto.analysis.errors.AbstractError;
import crypto.reporting.CommandLineReporter;
import crypto.rules.CryptSLRule;
import crypto.rules.CryptSLRuleReader;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.BreakpointStmt;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.queue.QueueReader;

public class CogniCryptAndroid {


	public static void run(String apkFile, String rules) throws IOException {
		String apkFile = args[0];
		String rules = args[2];

		try {
			InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
			config.getAnalysisFileConfig().setAndroidPlatformDir(args[1]);
			config.getAnalysisFileConfig().setTargetAPKFile(apkFile);
			SetupApplication infoflow = new SetupApplication(config);
			infoflow.constructCallgraph();
		} catch (Exception e) {
			AnalysisHelper.reportCallGraphError("CallGraphExceptions.txt", "CallGaph crashed on %s" , apkFile);
			return;
		}

		System.out.println("Analyzing " + apkFile);

		try {
			runCryptoAnalysis(apkFile, rules);
		} catch (Exception e) {
			AnalysisHelper.reportCallGraphError("CryptoAnalysisExceptions.txt", "CryptoAnalysis crashed on %s", apkFile);
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
