package main;

import boomerang.callgraph.ObservableDynamicICFG;
import boomerang.callgraph.ObservableICFG;
import boomerang.preanalysis.BoomerangPretransformer;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import crypto.analysis.CryptoScanner;
import crypto.rules.CryptSLRule;
import crypto.rules.CryptSLRuleReader;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SingleExecutor {

    private static ObservableICFG icfg;
    private static File apkFile;
    private static long callGraphTime;
    private static String RESOURCE_PATH = "rules/";
    private static Set<ErrorFilter> filters = Sets.newHashSet();
    private static CryptoScanner scanner;

    public static AnalysisResults run(String app, String rules, String path) throws InterruptedException, IOException {
        apkFile = new File(app);
        RESOURCE_PATH = rules;
        Stopwatch callGraphWatch = Stopwatch.createStarted();

        try {
            InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
            config.getAnalysisFileConfig().setAndroidPlatformDir(path);
            config.getAnalysisFileConfig().setTargetAPKFile(app);
            SetupApplication infoflow = new SetupApplication(config);
            infoflow.constructCallgraph();
        } catch (Exception e) {
            PrintWriter writer = new PrintWriter(new FileOutputStream(new File("CallGraphGenerationExceptions.txt"), true));
            writer.format("FlowDroid call graph generation crashed on %s", apkFile);
            e.printStackTrace(writer);
            writer.close();
            e.printStackTrace();
            return null;
        }
        callGraphTime = callGraphWatch.elapsed(TimeUnit.MILLISECONDS);

        scanner = new CryptoScanner() {
            @Override
            public boolean isCommandLineMode() {
                return true;
            }

            @Override
            public ObservableICFG<Unit, SootMethod> icfg() {
                return icfg;
            }

            public boolean rulesInSrcFormat() {
                return false;
            }
        };

        PureReporter report = new PureReporter(apkFile.getName(), getRules(), callGraphTime);

        scanner.getAnalysisListener().addReportListener(report);
        scanner.scan(getRules());

        return report.getResult();
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


}