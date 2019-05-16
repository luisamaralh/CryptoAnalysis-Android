package main;

import boomerang.callgraph.ObservableDynamicICFG;
import boomerang.callgraph.ObservableICFG;
import boomerang.preanalysis.BoomerangPretransformer;
import com.google.common.collect.Lists;
import crypto.analysis.CryptoScanner;
import crypto.rules.CryptSLRule;
import crypto.rules.CryptSLRuleReader;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class AnalysisHelper {

    public static void initializeInfoFlow(String app, String platform) {
        InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
        config.getAnalysisFileConfig().setAndroidPlatformDir(platform);
        config.getAnalysisFileConfig().setTargetAPKFile(app);
        SetupApplication infoflow = new SetupApplication(config);
        infoflow.constructCallgraph();
    }

    public static void reportCallGraphError(String errorFile, String message, String apk) {
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(new File(errorFile), true));
            writer.format(message, apk);
            writer.close();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CryptoScanner createCryptoScanner() {
        BoomerangPretransformer.v().reset();
        BoomerangPretransformer.v().apply();
        ObservableICFG icfg = new ObservableDynamicICFG(false);

        return new CryptoScanner() {
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
    }

    public static List<CryptSLRule> getRules(String resourcePath) {
        LinkedList<CryptSLRule> rules = Lists.newLinkedList();

        File[] listFiles = new File(resourcePath).listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".cryptslbin")) {
                rules.add(CryptSLRuleReader.readFromFile(file));
            }
        }
        return rules;
    }
}
