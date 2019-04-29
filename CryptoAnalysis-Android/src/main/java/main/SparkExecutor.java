package main;

import com.google.common.collect.Sets;
import com.google.common.io.Files;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SparkExecutor {

    private static int processors = Runtime.getRuntime().availableProcessors();
    private static String platformsDir;
    private static String rulesDir;
    private static String appsDir;
    private static Set<File> started = Sets.newHashSet();
    private static SingleExecutor execute;
    private static ThreadPoolExecutor executor;
    private static int timeoutTime;
    private static LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();;
    private static PureReporter pureReporter;

    public static void main(String... args) throws InterruptedException{


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
        executor = new ThreadPoolExecutor(processors-1, processors, timeoutTime, TimeUnit.MINUTES,workQueue);
        startProcesses();
    }

    private static void startProcesses() {
        File[] listFiles = new File(appsDir).listFiles();
        for (final File file : listFiles) {
            if(!started.add(file))
                continue;
            if (file.getName().endsWith(".apk") || file.getName().endsWith(".APK")) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        startProcess(file);
                    }
                });
            }
        }
    }


    private static void startProcess(File file) {
       
        try {
            execute.run(file.getName(), rulesDir, platformsDir);
            System.out.println(pureReporter.getResult());
            startProcesses();
        } catch (IOException ex) {
            System.err.println("Could not execute timeout command: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.err.println("Process was interrupted: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

}
