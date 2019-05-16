package main;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SparkExecutor implements IExecutor {


    public void run(ExecutorData data) {
        startProcesses(data.getPlatform(), data.getRulesDir(), data.getAppDir());
    }

    private static void startProcesses(String platformsDir, String rulesDir, String appsDir){
        long startTime = System.currentTimeMillis();
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
            JavaRDD<AnalysisResults> rddResults = rddFiles.map(f -> SingleExecutor.run(platformsDir, rulesDir, f.getAbsolutePath()))
                                                          .filter(r -> r != null);

            Long time = rddResults.map(r -> r.getTime()).fold(new Long(0), (t1, t2) -> t1 + t2);
            List<String> summary = rddResults.map(r -> r.getApplication() + ", " + r.getErrors().size() + ", " + r.getTime()).collect();

            ctx.stop();

            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;

            summary.forEach(s -> System.out.println(s));
            System.out.println("Total analysis time " + time);
            System.out.println("Execution time in milliseconds: " + timeElapsed);
        }
        catch(Exception e) {
            System.err.println("Error processing the apks");
            System.err.println("cause: " + e.getMessage());
        }
    }
}
