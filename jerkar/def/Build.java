import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jerkar.api.file.JkPathTree;
import org.jerkar.api.java.JkJavaProcess;
import org.jerkar.api.system.JkLog;
import org.jerkar.api.system.JkProcess;
import org.jerkar.api.utils.JkUtilsString;
import org.jerkar.api.utils.JkUtilsTime;
import org.jerkar.tool.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@JkImport("org.eclipse.jgit:org.eclipse.jgit:5.3.1.201904271842-r")
@JkImport("org.slf4j:slf4j-simple:jar:1.7.25")
class Build extends JkRun {

    String gitUrl = "https://github.com/jerkar/jerkar.github.io";

    JkPathTree jbakeSrc = getBaseTree().goTo("src/jbake");

    JkPathTree jbakeToolDir = getBaseTree().goTo("jerkar/tools/jbake-2.3.2");

    Path temp = getOutputDir().resolve("temp");

    Path targetSiteDir = getOutputDir().resolve("site");

    Path jerkarProjectPath = getBaseTree().getRoot().getParent().resolve("jerkar/org.jerkar.core");

    public Path gitRepoDir = getOutputDir().resolve("gitRepo");

    public String gitUsername = "djeang";

    public String gitPwd = "";

    public static void main(String[] args) throws IOException {
        JkInit.instanceOf(Build.class, args).full();
    }

    @JkDoc("Cleans the output directory except the compiled run classes.")
    public void clean() {
        JkLog.info("Clean output directory " + getOutputDir());
        if (Files.exists(getOutputDir())) {
            JkPathTree.of(getOutputDir())
                    .andMatching(false, JkConstants.DEF_BIN_DIR + "/**")
                    .andMatching(false, gitRepoDir.getFileName().toString())
                    .deleteContent();

        }
    }

    @JkDoc({ "Generates the site and imports documentation inside.",
            "You must have the Jerkar repo (containing the documentation) in your git home." })
    public void full() throws IOException {
        clean();
        makeJbakeTemp();
        addJbakeHeaders();
        jbake();
        copyJerkarDoc();
        copyCurrentJavadoc();
    }

    void makeJbakeTemp() {
        jbakeSrc.copyTo(temp);
    }

    void addJbakeHeaders() throws IOException {
        for (Path file : JkPathTree.of(temp).goTo("content").andMatching("*.md").getFiles()) {
            String content = jbakeHeader(file);
            byte[] previousContent = Files.readAllBytes(file);
            content = content + new String(previousContent, Charset.forName("UTF8"));
            Files.write(file, (content.getBytes(Charset.forName("UTF8"))), StandardOpenOption.CREATE);
        }
    }

    void copyJerkarDoc() {
        JkPathTree docTree = JkPathTree.of(jerkarProjectPath.resolve("jerkar/output/distrib/doc"));
        docTree.copyTo(targetSiteDir.resolve("doc"));
    }

    void copyCurrentJavadoc() {
        JkPathTree jerkarDistJavadoc = JkPathTree.of(jerkarProjectPath).goTo("org.jerkar.core/jerkar/output/javadoc");
        if (jerkarDistJavadoc.exists()) {
            jerkarDistJavadoc.copyTo(targetSiteDir.resolve("javadoc"));
        } else {
            JkLog.warn("Javadoc not found.");
        }
    }

    void jbake() {
        JkJavaProcess.of().withClasspath(jbakeToolDir.andMatching("lib/*.jar").getFiles())
                .runJarSync(jbakeToolDir.get("jbake-core.jar"), "jerkar/output/temp", "jerkar/output/site");
    }

    static String jbakeHeader(Path file) {
        String title = JkUtilsString.substringBeforeLast(file.getFileName().toString(), ".md");
        title = title.replace("_", " ").replace("-", " ");
        String template = "title=%s\ndate=%s\ntype=page\nstatus=published\n~~~~~~\n\n";
        return String.format(template, title, JkUtilsTime.now("yyyy-MM-dd"));
    }

    public void publish() {
        JkPathTree repoTree = JkPathTree.of(this.gitRepoDir);
        JkProcess git = JkProcess.of("git").withWorkingDir(gitRepoDir).withLogCommand(true).withFailOnError(true);
        if (!repoTree.goTo(".git").exists()) {
            repoTree.createIfNotExist();
            git.andParams("clone", gitUrl, ".").runSync();
        } else {
            git.andParams("pull").runSync();
        }
        repoTree.andMatching(false, ".git/**").deleteContent();
        JkPathTree.of(targetSiteDir).copyTo(repoTree.getRoot());
        git.andParams("add", "*").runSync();
        git.andParams("commit", "-am", "Doc").withFailOnError(false).runSync();
        git.andParams("push").runSync();
    }

    public void publish2() throws Exception {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(gitUsername, gitPwd);
        final Git git;
        if (Files.exists(gitRepoDir.resolve("./.git"))) {
            git = Git.open(gitRepoDir.toFile());
        } else {
            JkLog.startTask("Cloning git repo");
            git = Git.cloneRepository().setURI(gitUrl).setDirectory(gitRepoDir.toFile()).setBranch("master").call();
            JkLog.endTask();
        }
        git.rm().setCached(false).addFilepattern("jerkar/output/gitRepo/*").call();
        JkPathTree.of(gitRepoDir).andMatching(false, ".git/**").deleteContent();
        //JkPathTree.of(targetSiteDir).copyTo(gitRepoDir);
        git.add().addFilepattern("jerkar/output/gitRepo/*").call();
        RevCommit revCommit = git.commit().setMessage("doc").call();
        System.out.println(revCommit.getFullMessage());
        git.push().setCredentialsProvider(credentialsProvider).call();
        git.close();
    }
}
