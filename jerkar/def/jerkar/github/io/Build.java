package jerkar.github.io;

import org.jerkar.api.file.JkPathFile;
import org.jerkar.api.file.JkPathTree;
import org.jerkar.api.java.JkJavaProcess;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.api.utils.JkUtilsTime;

import org.jerkar.tool.JkDoc;
import org.jerkar.tool.JkInit;
import org.jerkar.tool.JkRun;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

class Build extends JkRun {

    JkPathTree jbakeSrc = getBaseTree().goTo("src/jbake");

    JkPathTree jbakeToolDir = getBaseTree().goTo("jerkar/tools/jbake-2.3.2");

    Path temp = getOutputDir().resolve("temp");

    Path targetSiteDir = getOutputDir().resolve("site");

    Path jerkarProjectPath = getBaseTree().getRoot().getParent().resolve("jerkar/org.jerkar.core");


    @JkDoc({ "Generates the site and imports documentation inside.",
            "You must have the Jerkar repo (containing the documentation) in your git home." })
    public void full() throws IOException {
        clean();
        makeJbakeTemp();
        addJbakeHeaders();
        jbake();
        copyCurrentDist();
        copyCurrentJavadoc();
    }

    private void makeJbakeTemp() {
        jbakeSrc.copyTo(temp);
        Path target = temp.resolve("content");
        JkPathTree.of(jerkarProjectPath).goTo("src/main/doc").andAccept("*.md").copyTo(target);
    }

    public void addJbakeHeaders() throws IOException {
        JkPathTree filesWithoutJbakeHeader = jbakeSrc.goTo("content").andAccept(("**/*.md"));
        for (Path file : filesWithoutJbakeHeader.getFiles()) {
            String content = jbakeHeader(file);
            byte[] previousContent = Files.readAllBytes(file);
            content = content + new String(previousContent, Charset.forName("UTF8"));
            Files.write(file, (content.getBytes(Charset.forName("UTF8"))), StandardOpenOption.CREATE);
        }
    }

    public void copyCurrentDist() {
        Path siteDistDir = targetSiteDir.resolve("binaries");
        Path jerkarDistZip =jerkarProjectPath.resolve("jerkar/output/org.jerkar.core-distrib.zip");
        JkPathTree.of(siteDistDir).bring(jerkarDistZip, StandardCopyOption.REPLACE_EXISTING);
    }

    public void copyCurrentJavadoc() {
        JkPathTree jerkarDistJavadoc = JkPathTree.of(jerkarProjectPath).goTo("org.jerkar.core/jerkar/output/javadoc");
        if (jerkarDistJavadoc.exists()) {
            JkLog.execute("Copping javadoc", () -> jerkarDistJavadoc.copyTo(targetSiteDir.resolve("javadoc")));
        } else {
            JkLog.warn("Javadoc not found.");
        }
    }

    public void jbake() {
        JkJavaProcess.of().withClasspath(jbakeToolDir.andAccept("lib/*.jar").getFiles())
                .runJarSync(jbakeToolDir.get("jbake-core.jar"),
                "jerkar/output/temp", "jerkar/output/site");
    }

    private static String jbakeHeader(Path file) {
        String title = JkUtilsString.substringBeforeLast(file.getFileName().toString(), ".md");
        title = title.replace("_", " ");
        StringBuilder result = new StringBuilder();
        result.append("title=").append(title).append("\n").append("date=" + JkUtilsTime.now("yyyy-MM-dd")).append("\n")
                .append("type=page\n").append("status=published\n");
        result.append("~~~~~~\n\n");
        return result.toString();
    }

    public static void main(String[] args) throws IOException {
        JkInit.instanceOf(Build.class, args).full();
    }



}
