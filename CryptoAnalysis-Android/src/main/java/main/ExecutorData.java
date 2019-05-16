package main;

public class ExecutorData {
    private String platform;
    private String appDir;
    private String rulesDir;
    private int timeOut;

    public ExecutorData(String platform, String rulesDir, String appDir) {
        this(platform, rulesDir, appDir,30);
    }

    public ExecutorData(String platform, String rulesDir, String appDir, int timeOut) {
        this.platform = platform;
        this.rulesDir = rulesDir;
        this.appDir = appDir;
        this.timeOut = timeOut;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAppDir() {
        return appDir;
    }

    public String getRulesDir() {
        return rulesDir;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public String toString() {
        return String.format("platform %s / rules %s / apps %s", platform, rulesDir, appDir);
    }
}
