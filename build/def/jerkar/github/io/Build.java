package jerkar.github.io;

import org.jerkar.api.file.JkPathTree;
import org.jerkar.api.java.JkJavaProcess;
import org.jerkar.api.java.JkManifest;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.api.utils.JkUtilsTime;
import org.jerkar.tool.JkBuild;
import org.jerkar.tool.JkDoc;
import org.jerkar.tool.JkInit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.jar.Attributes.Name;

class Build extends JkBuild {

    JkPathTree src = baseTree().goTo("src");
    JkPathTree markdownSrc = src.goTo("markdown");
    JkPathTree jbakeSrc = src.goTo("jbake");

    JkPathTree jbakeToolDir = baseTree().goTo("build/tools/jbake-2.3.2");
    JkPathTree siteBase = JkPathTree.of(outputDir().resolve("site"));

    // Source elements got from Jerkar project
    Path jerkarProjectPath = baseDir().getParent().resolve("jerkar");
    JkPathTree jerkarCoreDocDir = JkPathTree.of(jerkarProjectPath.resolve("org.jerkar.core/src/main/doc"));
    JkPathTree jerkarDistJavadoc = JkPathTree.of(jerkarProjectPath.resolve("org.jerkar.distrib-all/build/output/javadoc-all"));
    Path jerkarDistZip =jerkarProjectPath.resolve("org.jerkar.distrib-all/build/output/jerkar-distrib.zip");

    JkPathTree jbakeSrcContent = jbakeSrc.goTo("content");
    JkPathTree siteSourceDocDir = jbakeSrcContent.goTo("documentation");
    Path siteDistDir = siteBase.root().resolve("binaries");
    JkPathTree siteTargetDocDir = siteBase.goTo("content/documentation");

    JkPathTree filesWithoutJbakeHeader = jbakeSrcContent.accept("**/*.md").refuse("about.md", "download.md",
            "tell-me-more.md");

    JkPathTree filesToAddSideMenu = filesWithoutJbakeHeader.refuse("documentation/latest/faq.md");

    @Override
    public void clean() {
        siteBase.refuse(".*/**", "_*/**", "binaries/**").deleteContent();
        jbakeSrcContent.createIfNotExist().deleteContent();
    }

    @Override
    public void doDefault() {
        try {
            full();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        List<Path> files = jerkarCoreDocDir.accept("**/*.md").refuse("reference/**/*").files();
        Path temp = Files.createTempFile("reference", ".md");
        jerkarCoreDocDir.goTo("reference").copyIn(temp);
        files.add(temp);
        for (Path file : files) {
            String relativePath = file.startsWith(jerkarCoreDocDir.root()) ?
                    jerkarCoreDocDir.root().relativize(file).toString() : file.getFileName().toString();
            Path copied = targetDocDir.root().resolve(relativePath);
            Files.createDirectories(copied.getParent());
            JkLog.info("Importing doc file " + file + " to " + copied);
            byte[] content = Files.readAllBytes(file);
            Files.write(copied, content);
        }
        Files.deleteIfExists(temp);
    }

    public void addJbakeHeaders() throws IOException {
        for (Path file : filesWithoutJbakeHeader.files()) {
            String content = jbakeHeader(file);
            byte[] previousContent = Files.readAllBytes(file);
            content = content + new String(previousContent, Charset.forName("UTF8"));
            Files.write(file, (content.getBytes(Charset.forName("UTF8"))), StandardOpenOption.CREATE);
        }
    }

    public void importSiteDoc() throws IOException {
        Path dest = jbakeSrc.get("content");
        for (Path path : markdownSrc.files()) {
            String originalContent = new String(Files.readAllBytes(path), Charset.forName("UTF8"));
            String newContent = originalContent.replace("${jerkarVersion}", currentJerkarVersion());
            Path targetFile = dest.resolve(markdownSrc.root().relativize(path));
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, newContent.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE_NEW);
        }
    }

    public void copyCurrentDist() {
        JkPathTree.of(siteDistDir).copyIn(jerkarDistZip);

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
        jerkarDistJavadoc.copyTo(siteBase.get("javadoc/latest"));
    }

    public void jbake() {
        JkJavaProcess.of().withClasspaths(jbakeToolDir.accept("lib.*.jar").files()).runJarSync(jbakeToolDir.get("jbake-core.jar"),
                "src/jbake", "build/output/site");
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

    public static void main(String[] args) {
        JkInit.instanceOf(Build.class, args).doDefault();
    }

    private void addMenu() throws IOException {
        for (Path file : filesToAddSideMenu.files()) {
            String menuHtml = ImplicitMenu.ofMarkdowndFile(file, 2).divSideBarAndScriptHtml();
            byte[] currentContent = Files.readAllBytes(file);
            Files.write(file, menuHtml.getBytes("UTF8"), StandardOpenOption.CREATE);
            Files.write(file, currentContent, StandardOpenOption.APPEND);
            Files.write(file, ImplicitMenu.endDivHtml("end of wrapper div").getBytes(Charset.forName("UTF8")), StandardOpenOption.APPEND);
        }
    }

}
