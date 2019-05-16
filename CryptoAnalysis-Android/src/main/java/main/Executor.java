package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.maven.shared.scriptinterpreter.RunErrorException;


public class Executor implements IExecutor {
    private static int processors = Runtime.getRuntime().availableProcessors();
    private static LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
	private static Set<File> started = Sets.newHashSet();
	private static ThreadPoolExecutor executor;

	private static final int TIMEOUT = 30;

	public void run(ExecutorData data) {
		long startTime = System.currentTimeMillis();
		startProcesses(data.getPlatform(), data.getRulesDir(), data.getAppDir(), data.getTimeOut());
		try {
			int iteration = 1;
			while(executor.getActiveCount() > 0) {
				Thread.sleep(5000);
				System.out.println("running: " + executor.getActiveCount());
				System.out.println("seconds waiting: " + iteration * 5000);
				iteration++;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println(timeElapsed);
		System.exit(0);
	}

	public static void startProcesses(String platformDir, String rulesDir, String appsDir, Integer timeoutTime) {
        executor = new ThreadPoolExecutor(processors - 1, processors, timeoutTime, TimeUnit.MINUTES, workQueue);
        File[] listFiles = new File(appsDir).listFiles();
        for (final File file : listFiles) {
            if (!started.add(file))
                continue;
            if (file.getName().endsWith(".apk") || file.getName().endsWith(".APK")) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        startProcess(file, appsDir, platformDir, rulesDir, timeoutTime);
                    }
                });
            }
        }
	}

	private static void startProcess(File file, String appsDir, String platformsDir, String rulesDir, Integer timeoutTime) {
		String classpath = System.getProperty("java.class.path");
		String javaHome = System.getProperty("java.home");
		String[] command = new String[] { javaHome + File.separator + "bin" + File.separator + "java", "-Xmx8g", "-Xms1g",
				"-Xss64m", "-cp", classpath, CogniCryptAndroid.class.getName(), platformsDir, rulesDir, file.getAbsolutePath()};
		System.out.println("Running command: " + Arrays.toString(command));
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			File reportsDir = new File("target/reports/");
			if (!reportsDir.exists())
				reportsDir.mkdirs();
			pb.redirectOutput(new File("target/reports/" + file.getName() + "-out.txt"));
			pb.redirectError(new File("target/reports/" + file.getName() + "-err.txt"));
			Process proc = pb.start();
			boolean finished = proc.waitFor(timeoutTime, TimeUnit.MINUTES);
			if (!finished) {
				proc.destroy();
				proc.waitFor(); // wait for the process to terminate

				String folder = file.getParent();
				String analyzedFolder = folder + File.separator + "timedout";
				File dir = new File(analyzedFolder);
				if (!dir.exists()) {
					dir.mkdir();
				}
				File moveTo = new File(dir.getAbsolutePath() + File.separator + file.getName());
				Files.move(file, moveTo);
			}
			startProcesses(appsDir, platformsDir, rulesDir, timeoutTime);
		} catch (IOException ex) {
			System.err.println("Could not execute timeout command: " + ex.getMessage());
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			System.err.println("Process was interrupted: " + ex.getMessage());
			ex.printStackTrace();
		}

	}

}
