package main;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SparkExecutor {
    private static String platformsDir;
    private static String rulesDir;
    private static String appsDir;
    private static int timeoutTime;

    public static void main(String... args) {
        if(args.length == 0){
            System.out.println("This prorgam expects four arguments in this order: \n");
            System.out.println("1. the path to the android platform directory \n");
            System.out.println("2. the path to the folder containting the android applications to be analyzed \n");
            System.out.println("3. the path to CrySL rules \n");
        }
        platformsDir = args[0];
        appsDir = args[1];
        rulesDir = args[2];
        timeoutTime = Integer.parseInt(args[3]);
        startProcesses();
    }

    private static void startProcesses() {
        List<File> listFiles = Arrays.stream(new File(appsDir).listFiles())
                .filter(file -> file.getName().endsWith(".apk") || file.getName().endsWith(".APK"))
                .collect(Collectors.toList());

        listFiles.stream().forEach(apk -> {
            try {
                AnalysisResults res = SingleExecutor.run(apk.getAbsolutePath(), rulesDir, platformsDir);
                System.out.println("Application: " + res.getApplication());
                System.out.println("Analysis time: " + res.getTime());
                System.out.println("Erros: " + res.getErrors().size());
            }
            catch(Exception e) {
                System.err.println("Error processing " + apk.getName());
                System.err.println("cause: " + e.getMessage());
            }
        });
    }
}
