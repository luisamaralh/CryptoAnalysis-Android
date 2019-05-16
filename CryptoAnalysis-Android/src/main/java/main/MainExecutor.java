package main;

import com.google.common.collect.Sets;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import scala.Int;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainExecutor implements CommandLineParser{


    private static int processors = Runtime.getRuntime().availableProcessors();
    private static String platformsDir;
    private static int timeoutTime;
    private static LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();;
    private static Set<File> started = Sets.newHashSet();
    private static ThreadPoolExecutor threadPoolExecutor;
    private static String rulesDir;
    private static String appsDir;

    public static void main(option, String[]) throws InterruptedException {

        SparkExecutor sparkExecutor = new SparkExecutor();
        Executor executor = new Executor();
        CogniCryptAndroid cogniCryptAndroid = new CogniCryptAndroid();


        Options options = new Options();
        options.addOption("e", "SingleExecutor", true, "Single execution.");
        options.addOption("m", "MultipleExecultor", true, "Multiple Execution");
        options.addOption("s", "SparkExecultor", true, "Spark Execution");


        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MainExecutor", options);


        if (args.length == 0) {
            System.out.println("This prorgam expects four arguments in this order: \n");
            System.out.println("1. the path to the android platform directory \n");
            System.out.println("2. the path to the folder containting the android applications to be analyzed \n");
            System.out.println("3. the path to CrySL rules \n");
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        platformsDir = args[1];
        appsDir = args[2];
        rulesDir = args[3];
        timeoutTime = Integer.parseInt(args[4]);

        System.out.println(platformsDir);
        System.out.println(rulesDir);
        System.out.println(appsDir);
        System.out.println(timeoutTime);

        if ("s".equals(options.toString())) {
            sparkExecutor.runSpark(platformsDir, appsDir, rulesDir);

            System.out.println("Passei aqui: Spark executor ");
            threadPoolExecutor = new ThreadPoolExecutor(processors - 1, processors, timeoutTime, TimeUnit.MINUTES, workQueue);
            executor.runExecutor(platformsDir, appsDir, rulesDir, timeoutTime);
            threadPoolExecutor.awaitTermination(30, TimeUnit.DAYS);

            try {
                cogniCryptAndroid.run(appsDir, platformsDir, rulesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("m".equals(options)) {
            System.out.println("Passei aqui: Multiple Executor");
            threadPoolExecutor = new ThreadPoolExecutor(processors - 1, processors, timeoutTime, TimeUnit.MINUTES, workQueue);
            executor.runExecutor(platformsDir, appsDir, rulesDir, timeoutTime);
            threadPoolExecutor.awaitTermination(30, TimeUnit.DAYS);

            try {
                cogniCryptAndroid.run(appsDir, platformsDir, rulesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("e".equals(options)) {
            System.out.println("Passei aqui: Single Executor");
            try {
                cogniCryptAndroid.run(appsDir, platformsDir, rulesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
