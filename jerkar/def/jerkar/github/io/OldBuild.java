package jerkar.github.io;

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

class OldBuild extends JkRun {

    JkPathTree src = getBaseTree().goTo("src");
    JkPathTree markdownSrc = src.goTo("markdown");
    JkPathTree jbakeSrc = src.goTo("jbake");

    JkPathTree jbakeToolDir = getBaseTree().goTo("jerkar/tools/jbake-2.3.2");
    JkPathTree siteBase = JkPathTree.of(getOutputDir().resolve("site"));

    Path temp = getOutputDir().resolve("temp");

    // Source elements got from Jerkar project
    Path jerkarProjectPath = getBaseTree().getRoot().getParent().resolve("jerkar/org.jerkar.core");

    JkPathTree jerkarDistJavadoc = JkPathTree.of(jerkarProjectPath).goTo("org.jerkar.core/jerkar/output/javadoc");
    Path jerkarDistZip =jerkarProjectPath.resolve("jerkar/output/org.jerkar.core-distrib.zip");

    JkPathTree jbakeSrcContent = jbakeSrc.goTo("content");
    //JkPathTree siteSourceDocDir = jbakeSrcContent.goTo("documentation");
    Path siteDistDir = siteBase.getRoot().resolve("binaries");
    JkPathTree siteTargetDocDir = siteBase.goTo("content/documentation");

    JkPathTree filesWithoutJbakeHeader = jbakeSrcContent.andAccept(("**/*.md")).andReject("about.md", "download.md",
            "tell-me-more.md");

    JkPathTree filesToAddSideMenu = filesWithoutJbakeHeader.andReject("documentation/latest/faq.md");

    @JkDoc({ "Generates the site and imports documentation inside.",
            "You must have the Jerkar repo (containing the documentation) in your git home." })
    public void full() throws IOException {
        clean();
        makeJbakeTemp();
        //addMenu();
        addJbakeHeaders();
        jbake();
        copyCurrentDist();
        copyCurrentJavadoc();
    }

    private void makeJbakeTemp() {
        jbakeSrc.copyTo(temp);

        // import .md files from Jerkar project
        Path target = temp.resolve("content");
        JkPathTree.of(jerkarProjectPath).goTo("src/main/doc").andAccept("*.md").copyTo(target);
    }

    public void addJbakeHeaders() throws IOException {
        for (Path file : filesWithoutJbakeHeader.getFiles()) {
            String content = jbakeHeader(file);
            byte[] previousContent = Files.readAllBytes(file);
            content = content + new String(previousContent, Charset.forName("UTF8"));
            Files.write(file, (content.getBytes(Charset.forName("UTF8"))), StandardOpenOption.CREATE);
        }
    }

    public void importSiteDoc() throws IOException {
        Path dest = jbakeSrc.get("content");
        for (Path path : markdownSrc.getFiles()) {
            String originalContent = new String(Files.readAllBytes(path), Charset.forName("UTF8"));
            String newContent = originalContent.replace("${jerkarVersion}", currentJerkarVersion());
            Path targetFile = dest.resolve(markdownSrc.getRoot().relativize(path));
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, newContent.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE_NEW);
        }
    }

    public void copyCurrentDist() {
        JkPathTree.of(siteDistDir)
                .bring(jerkarDistZip, StandardCopyOption.REPLACE_EXISTING);
    }

    private String currentJerkarVersion() {
        return "0.0.7";
        /*
        JkLog.info("Read manifest from " + jerkarDistZip);
        JkPathTree zipFile = JkPathTree.ofZip(jerkarDistZip);
        Path manifestPath  = zipFile.get("META-INF/MANIFEST.MF");
        JkManifest manifest = JkManifest.of(manifestPath);
        return manifest.mainAttribute(Name.IMPLEMENTATION_VERSION);
        */

    }

    public void copyCurrentJavadoc() {
        if (jerkarDistJavadoc.exists()) {
            JkLog.execute("Copping javadoc", () -> jerkarDistJavadoc.copyTo(siteBase.get("javadoc")));
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
        JkInit.instanceOf(OldBuild.class, args).full();
    }

    private void addMenu() throws IOException {
        for (Path file : filesToAddSideMenu.getFiles()) {
            String menuHtml = ImplicitMenu.ofMarkdowndFile(file, 2).divSideBarAndScriptHtml();
            byte[] currentContent = Files.readAllBytes(file);
            Files.write(file, menuHtml.getBytes("UTF8"), StandardOpenOption.CREATE);
            Files.write(file, currentContent, StandardOpenOption.APPEND);
            Files.write(file, ImplicitMenu.endDivHtml("end of wrapper div").getBytes(Charset.forName("UTF8")),
                    StandardOpenOption.APPEND);
        }
    }

}
