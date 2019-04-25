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
import java.util.List;

class Build extends JkRun {

    JkPathTree src = getBaseTree().goTo("src");
    JkPathTree markdownSrc = src.goTo("markdown");
    JkPathTree jbakeSrc = src.goTo("jbake");

    JkPathTree jbakeToolDir = getBaseTree().goTo("jerkar/tools/jbake-2.3.2");
    JkPathTree siteBase = JkPathTree.of(getOutputDir().resolve("site"));

    // Source elements got from Jerkar project
    Path jerkarProjectPath = getBaseTree().getRoot().getParent().resolve("jerkar");
    JkPathTree jerkarCoreDocDir = JkPathTree.of(jerkarProjectPath.resolve("org.jerkar.core/src/main/doc"));
    JkPathTree jerkarDistJavadoc = JkPathTree.of(jerkarProjectPath.resolve("org.jerkar.core/jerkar/output/javadoc-all"));
    Path jerkarDistZip =jerkarProjectPath.resolve("org.jerkar.core/jerkar/output/org.jerkar.core-distrib.zip");

    JkPathTree jbakeSrcContent = jbakeSrc.goTo("content");
    JkPathTree siteSourceDocDir = jbakeSrcContent.goTo("documentation");
    Path siteDistDir = siteBase.getRoot().resolve("binaries");
    JkPathTree siteTargetDocDir = siteBase.goTo("content/documentation");

    JkPathTree filesWithoutJbakeHeader = jbakeSrcContent.andAccept(("**/*.md")).andReject("about.md", "download.md",
            "tell-me-more.md");

    JkPathTree filesToAddSideMenu = filesWithoutJbakeHeader.andReject("documentation/latest/faq.md");

    @Override
    public void clean() {
        siteBase.andReject(".*/**", "_*/**", "binaries/**").deleteContent();
        jbakeSrcContent.createIfNotExist().deleteContent();
    }

    @JkDoc({ "Generates the site and imports documentation inside.",
            "You must have the Jerkar repo (containing the documentation) in your git home." })
    public void full() throws IOException {
        clean();
        importContent();
        addMenu();
        addJbakeHeaders();
        jbake();
        copyCurrentDist();
        copyCurrentJavadoc();
    }

    public void importContent() throws IOException {
        importDocFromJerkarProject();
        importSiteDoc();
    }

    public void importDocFromJerkarProject() throws IOException {
        JkPathTree targetDocDir = siteSourceDocDir.goTo("latest");
        List<Path> files = jerkarCoreDocDir.andAccept("**/*.md").andReject("reference/**/*").getFiles();
        Path temp = Files.createTempFile("reference", ".md");
        jerkarCoreDocDir.goTo("reference").bring(temp);
        files.add(temp);
        for (Path file : files) {
            String relativePath = file.startsWith(jerkarCoreDocDir.getRoot()) ?
                    jerkarCoreDocDir.getRoot().relativize(file).toString() : file.getFileName().toString();
            Path copied = targetDocDir.getRoot().resolve(relativePath);
            Files.createDirectories(copied.getParent());
            JkLog.info("Importing doc file " + file + " to " + copied);
            byte[] content = Files.readAllBytes(file);
            Files.write(copied, content);
        }
        Files.deleteIfExists(temp);
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
        JkLog.execute("copying current dir",() -> JkPathTree.of(siteDistDir)
                .bring(jerkarDistZip, StandardCopyOption.REPLACE_EXISTING));


        // Retrieve the current version

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
            JkLog.execute("Copping javadoc", () -> jerkarDistJavadoc.copyTo(siteBase.get("javadoc/latest")));
        } else {
            JkLog.warn("Javadoc not found.");
        }
    }

    public void jbake() {
        JkJavaProcess.of().withClasspath(jbakeToolDir.andAccept("lib/*.jar").getFiles())
                .runJarSync(jbakeToolDir.get("jbake-core.jar"),
                "src/jbake", "jerkar/output/site");
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
