import org.jerkar.tool.JkInit;

class RunPublish {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        JkInit.instanceOf(Build.class, args).publish();
    }

}
