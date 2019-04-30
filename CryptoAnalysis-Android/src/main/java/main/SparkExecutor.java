package main;

import fj.data.Java;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

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
        try {
            Logger.getLogger("org").setLevel(Level.OFF);
            Logger.getLogger("akka").setLevel(Level.OFF);

            //int cores = Runtime.getRuntime().availableProcessors();

            List<File> listFiles = Arrays.stream(new File(appsDir).listFiles())
                    .filter(file -> file.getName().endsWith(".apk") || file.getName().endsWith(".APK"))
                    .collect(Collectors.toList());

            //int tasksInParallel = listFiles.size() >= cores ? cores : listFiles.size();

            SparkConf sparkConf = new SparkConf()
                    .setAppName("Spark Executor for CogniCrypt")
                    .setMaster("local");

            JavaSparkContext ctx = new JavaSparkContext(sparkConf);
            ctx.setLogLevel("OFF");

            JavaRDD<File> rddFiles = ctx.parallelize(listFiles);
            JavaRDD<AnalysisResults> rddResults = rddFiles.map(f -> SingleExecutor.run(f.getAbsolutePath(), rulesDir, platformsDir));

            Long time = rddResults.map(r -> r.getTime()).fold(new Long(0), (t1, t2) -> t1 + t2);
            List<String> summary = rddResults.map(r -> r.getApplication() + ", " + r.getErrors().size() + ", " + r.getTime()).collect();

            ctx.stop();

            summary.forEach(s -> System.out.println(s));
            System.out.println("Total analysis time " + time);
        }
        catch(Exception e) {
            System.err.println("Error processing the apks");
            System.err.println("cause: " + e.getMessage());
        }
    }
}
