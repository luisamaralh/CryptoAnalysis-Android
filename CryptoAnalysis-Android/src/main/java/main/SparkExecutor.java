package main;

import com.google.common.collect.Sets;


import java.io.File;
import java.util.Set;

public class SparkExecutor {

    private static String platformsDir;
    private static String rulesDir;
    private static String appsDir;
    private static Set<File> started = Sets.newHashSet();
    private static SingleExecutor executor;

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
        startProcesses();
    }

    private static void startProcesses() {
        File[] listFiles = new File(appsDir).listFiles();
        for (final File file : listFiles) {
            if (file.getName().endsWith(".apk") || file.getName().endsWith(".APK")) {
                executor.run(file, );
            }

//            if(!started.add(file))
//                continue;
//            if (file.getName().endsWith(".apk") || file.getName().endsWith(".APK")) {
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        startProcess(file);
//                    }
//                });
//            }
        }
    }

//    private static void startProcess(File file) {
//        String classpath = System.getProperty("java.class.path");
//        String javaHome = System.getProperty("java.home");
//        String[] command = new String[] { javaHome + File.separator + "bin" + File.separator + "java", "-Xmx8g", "-Xms1g",
//                "-Xss64m", "-cp", classpath, CogniCryptAndroid.class.getName(), file.getAbsolutePath(), platformsDir, rulesDir };
//        System.out.println("Running command: " + Arrays.toString(command));
//        try {
//            ProcessBuilder pb = new ProcessBuilder(command);
//            File reportsDir = new File("target/reports/");
//            if (!reportsDir.exists())
//                reportsDir.mkdirs();
//            pb.redirectOutput(new File("target/reports/" + file.getName() + "-out.txt"));
//            pb.redirectError(new File("target/reports/" + file.getName() + "-err.txt"));
//            Process proc = pb.start();
//
//            startProcesses();
//        } catch (IOException ex) {
//            System.err.println("Could not execute timeout command: " + ex.getMessage());
//            ex.printStackTrace();
//        }
//
//    }
}
