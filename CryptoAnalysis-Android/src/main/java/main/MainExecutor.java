package main;

import com.google.common.collect.Sets;
import org.apache.commons.cli.*;
import scala.Int;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainExecutor {


    private static int processors = Runtime.getRuntime().availableProcessors();
    private static int timeoutTime;
    private static LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();;
    private static Set<File> started = Sets.newHashSet();
    private static ThreadPoolExecutor threadPoolExecutor;
    private String platformsDir;
    private String rulesDir;
    private String appsDir;
    private boolean singleMode;
    private Options options;


    private void createOptions() {
        options = new Options();

        Option e = Option.builder("e")
                        .required()
                        .longOpt("execution-mode")
                        .numberOfArgs(1)
                        .desc("one of single-executor, multiple-executor, or spark-executor").build();
        Option p = Option.builder()
                        .required()
                        .longOpt("platform-path")
                        .numberOfArgs(1)
                        .desc("path to the Android Platform")
                        .build();

        Option r = Option.builder()
                        .required()
                        .longOpt("rules-path")
                        .numberOfArgs(1)
                        .desc("path to the location of CrySL rules")
                        .build();

        Option a = Option.builder("a")
                .required()
                .longOpt("apk")
                .numberOfArgs(1)
                .desc("path to the app file or folder")
                .build();

        options.addOption(e);
        options.addOption(p);
        options.addOption(r);
        options.addOption(a);

    }
    public static void main(String[] args) {
        System.out.println("Running the executor");

        MainExecutor m = new MainExecutor();

        m.createOptions();

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(m.options, args);

            m.platformsDir = cmd.getOptionValue("platform-path");
            m.rulesDir = cmd.getOptionValue("rules-path");
            m.appsDir = cmd.getOptionValue("apk");

            ExecutorData data = new ExecutorData(m.platformsDir, m.rulesDir, m.appsDir);

            switch (cmd.getOptionValue("e")) {
                case "multiple-executor" : (new Executor()).run(data); break;
                case "spark-executor" : (new SparkExecutor()).run(data); break;
                default: (new CogniCryptAndroid()).run(data);
            }


        }
        catch(ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MainExecutor", m.options);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
