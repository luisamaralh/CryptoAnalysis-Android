package main;

import boomerang.callgraph.ObservableICFG;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import crypto.analysis.CryptoScanner;
import crypto.rules.CryptSLRule;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SingleExecutor {

    private static ObservableICFG icfg;
    private static File apkFile;
    private static long callGraphTime;
    private static Set<ErrorFilter> filters = Sets.newHashSet();
    private static CryptoScanner scanner;

    public static AnalysisResults run(String app, String rules, String platform) throws IOException {
        apkFile = new File(app);
        Stopwatch callGraphWatch = Stopwatch.createStarted();

        try {
            InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
            config.getAnalysisFileConfig().setAndroidPlatformDir(platform);
            config.getAnalysisFileConfig().setTargetAPKFile(app);
            SetupApplication infoflow = new SetupApplication(config);
            infoflow.constructCallgraph();
        } catch (Exception e) {
           AnalysisHelper.reportCallGraphError("CallGraphError.txt", "CallGraph crashes on %s", app);
            return null;
        }
        callGraphTime = callGraphWatch.elapsed(TimeUnit.MILLISECONDS);

        scanner = AnalysisHelper.createCryptoScanner();

        List<CryptSLRule> cslRules = AnalysisHelper.getRules(rules);
        SimpleReporter report = new SimpleReporter(apkFile.getName(), cslRules, callGraphTime);

        scanner.getAnalysisListener().addReportListener(report);
        scanner.scan(cslRules);

        return report.getResult();
    }
}