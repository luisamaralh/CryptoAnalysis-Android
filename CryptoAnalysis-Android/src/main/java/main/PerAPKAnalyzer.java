package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import boomerang.debugger.Debugger;
import crypto.analysis.CryptoScanner;
import crypto.analysis.errors.AbstractError;
import crypto.reporting.CommandLineReporter;
import crypto.rules.CryptSLRule;
import crypto.rules.CryptSLRuleReader;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.TestApps.Test;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import typestate.TransitionFunction;

public class PerAPKAnalyzer {

	private static boolean runCryptoScanner;
	private static boolean VISUALIZATION = false;
	private static Debugger<TransitionFunction> debugger = (VISUALIZATION ? null : new Debugger<TransitionFunction>());
	private static JimpleBasedInterproceduralCFG icfg;
	private static File ideVizFile;
	private static File apkFile;
	private static long callGraphTime;
	private static String RESOURCE_PATH = "rules/";
	private static Set<ErrorFilter> filters = Sets.newHashSet();
	private static CryptoScanner scanner;


	public static Debugger<TransitionFunction> getDebugger() {
		if (debugger == null) {
			if (!ideVizFile.getParentFile().exists()) {
				ideVizFile.getParentFile().mkdirs();
			}
//			debugger = new CryptoVizDebugger(ideVizFile, icfg);
		}
		return debugger;
	}

	public static void main(String... args) throws InterruptedException, IOException {
		apkFile = new File(args[0]);
		ideVizFile = new File("target/IDEViz/" + apkFile.getName().replace(".apk", ".viz"));
		RESOURCE_PATH = args[2];
		Stopwatch callGraphWatch = Stopwatch.createStarted();

		try {
			Test.main(new String[] { args[0], args[1], "--notaintanalysis", "--callbackanalyzer", "DEFAULT" });
		} catch (Exception e) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File("CallGraphGenerationExceptions.txt"), true));
			writer.format("FlowDroid call graph generation crashed on %s", apkFile);
			e.printStackTrace(writer);
			writer.close();
			String folder = apkFile.getParent();
			String analyzedFolder = folder + File.separator + "flowdroid-crashed";
			File dir = new File(analyzedFolder);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File moveTo = new File(dir.getAbsolutePath() + File.separator + apkFile.getName());
			Files.move(apkFile, moveTo);
			return;
		}
		callGraphTime = callGraphWatch.elapsed(TimeUnit.MILLISECONDS);

		System.out.println("Analyzing " + apkFile.getName());
		try {
			runCryptoAnalysis();
		} catch (Exception e) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File("CryptoAnalysisExceptions.txt"), true));
			writer.format("CryptoAnalysis crashed on %s", apkFile);
			e.printStackTrace(writer);
			writer.close();
		}
		String folder = apkFile.getParent();
		String analyzedFolder = folder + File.separator + "analyzed" + (runCryptoScanner ? "" : "-no-crypto");
		File dir = new File(analyzedFolder);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File moveTo = new File(dir.getAbsolutePath() + File.separator + apkFile.getName());
		Files.move(apkFile, moveTo);
	}

	protected static List<CryptSLRule> getRules() {
		LinkedList<CryptSLRule> rules = Lists.newLinkedList();

		File[] listFiles = new File(RESOURCE_PATH).listFiles();
		for (File file : listFiles) {
			if (file.getName().endsWith(".cryptslbin")) {
				rules.add(CryptSLRuleReader.readFromFile(file));
			}
		}
		return rules;
	}

	private static void runCryptoAnalysis() {
		icfg = new JimpleBasedInterproceduralCFG(false);
		scanner = new CryptoScanner(getRules()) {

			@Override
			public boolean isCommandLineMode() {
				return true;
			}

			@Override
			public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
				return icfg;
			}

		};
		
		
		addErrorReporter(new ErrorFilter("com.google."));
		addErrorReporter(new ErrorFilter("com.google."));
		addErrorReporter(new ErrorFilter("com.unity3d."));
		addErrorReporter(new ErrorFilter("com.facebook.ads."));
		addErrorReporter(new ErrorFilter("com.android."));
		File compFilterOutputFile = new File(getSummaryFile() + "Complement.csv");
		Complementfilter compFilter = new Complementfilter();
		scanner.getAnalysisListener().addReportListener(new FilteredCSVReporter(compFilter, compFilterOutputFile.getAbsolutePath(), apkFile.getName(), getRules(), callGraphTime));
		NoFilter emptyFilter = new NoFilter();
		File emptyFilterFile = new File(getSummaryFile() + "AllSeeds.csv");
		scanner.getAnalysisListener().addReportListener(new FilteredCSVReporter(emptyFilter, emptyFilterFile.getAbsolutePath(), apkFile.getName(), getRules(), callGraphTime));

		File detailedOutputFile = new File("target/reports/cognicrypt/" + apkFile.getName().replace(".apk", ".txt"));
		detailedOutputFile.getParentFile().mkdirs();
		scanner.getAnalysisListener().addReportListener(new CommandLineReporter(detailedOutputFile.getAbsolutePath(), getRules()));
		scanner.scan();
	}
	private static class NoFilter implements Predicate<AbstractError> {
		@Override
		public boolean test(AbstractError t) {
			return false;
		}

		@Override
		public String toString() {
			return "AllSeeds";
		}
}
	private static class Complementfilter implements Predicate<AbstractError> {
			@Override
			public boolean test(AbstractError t) {
				for (ErrorFilter f : filters)
					if (f.test(t))
						return true;
				return false;
			}

			@Override
			public String toString() {
				return "Complement";
			}
	}
	private static void addErrorReporter(ErrorFilter filter) {
		File file = new File(getSummaryFile() + filter.toString() + ".csv");
		scanner.getAnalysisListener().addReportListener(new FilteredCSVReporter(filter, file.getAbsolutePath(), apkFile.getName(), getRules(), callGraphTime));
		filters.add(filter);
	}

	private static String getSummaryFile() {
		String property = System.getProperty("SummaryFile");
		if (property != null)
			return property;
		return "summary-report";
	}

}
