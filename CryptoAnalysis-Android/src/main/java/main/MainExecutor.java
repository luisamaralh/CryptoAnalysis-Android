package main;

import com.google.common.collect.Sets;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import scala.Int;

import java.io.File;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainExecutor {


    private static int processors = Runtime.getRuntime().availableProcessors();
    private static String platformsDir;
    private static int timeoutTime;
    private static LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();;
    private static Set<File> started = Sets.newHashSet();
    private static ThreadPoolExecutor threadPoolExecutor;
    private static String rulesDir;
    private static String appsDir;

    public static void main(String... args) throws InterruptedException {

        SparkExecutor sparkExecutor = new SparkExecutor();
        Executor executor = new Executor();



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
        platformsDir = args[0];
        appsDir = args[1];
        rulesDir = args[2];
        timeoutTime = Integer.parseInt(args[3]);


        switch (options.toString()){

            case("s"):{
                sparkExecutor.runSpark(platformsDir, appsDir, rulesDir);
            }

            case("m"):{
                threadPoolExecutor = new ThreadPoolExecutor(processors-1, processors, timeoutTime, TimeUnit.MINUTES,workQueue);
                executor.runExecutor(platformsDir, appsDir, rulesDir, timeoutTime);
                threadPoolExecutor.awaitTermination(30, TimeUnit.DAYS);
            }

            case("e"):{

            }
        }


    }

}
