## What is it ?

**Jerkar** is a Java build tool ala ***Gradle***, ***Ant/Ivy*** or ***Maven***. It differs from the others in that it requires **pure Java code** instead of XML files or dynamic language scripts to describe builds.

## Into the box

* Complete Java build tool : Java compilation, resources processing, test execution/reporting, jar/war packaging
* Powerful dependency management fully compliant with *Maven* and *Ivy*
* Powerful multi-project support 
* Plugable architecture with builtin plugins for *Eclipse*, *IntelliJ*, *Jacoco* and *SonarQube*
* Multi-techno support for building projects embedding any other technology than Java.
* Swiss-knife library for performing all kind of build related stuff as file and I/O  manipulation, logging, PGP signing, external tool launcher, ...
* Multi-level configuration to fit in enterprise environment
* Hierarchical logs
* Ability to rely on convention only (no build script needed at all) or get build information from IDE meta-data
* Facility to migrate *Maven* projects
* Scaffolding feature for creating projects from scratch
 
Also as an **automation tool** Jerkar provides :

* The ability to treat Java sources as scripts : Java sources are **compiled on-the-fly** prior to be instantiated then method invoked
* **full dependency handling** to compile scripts : script dependencies can be made on Maven repositories, local jars or external projects

## Motivation

As a **Java developer** you may have already been frustrated of not being able to write your build scripts with your favorite language and, moreover, with the **same language as the project to build**.
Indeed most of mainstream and JVM languages have first class build tool where definitions are expressed using the language itself : **Gradle** for **Groovy**, **Nodejs** based tools for **Javascript**, **SBT** for **Scala**,  **PyBuilder** for **Python**, **Rake** for **Ruby**, **Leiningen** for **Clojure**, **Kobalt** for **Kotlin** ...
 
**Jerkar** purposes to fill the gap by providing a **full-featured build tool** allowing Java developers to build their projects by just writing **regular Java classes** as they are so familiar with. 

### Benefits

As said, with Jerkar, build definitions are **plain old java classes**. This bare metal approach brings concrete benefits :

* for Java developers, it's **trivial to add logic** in build scripts
* when editing build definition, Java developers leverage of **compilation**, **code-completion** and **debug** facilities provided by their **IDE**
* Java developers have **no extra language** or **XML soup** to master
* build definitions can be **launched/debugged** directly from the IDE as any class providing a Main method 
* the tool is quite **simple and fast** : in essence, Jerkar engine simply performs direct method invocations on build classes. **No black box** : it's quite easy to discover what the build is actually doing under the hood. **Jerkar source code and javadoc** are a primary source of documentation.
* scripts can directly leverage of any Java **3rd party libraries** without needing to wrap it in a plugin or a specific component
* it's straightforward to **extend**
* **refactoring** build definition is easy and safe (thanks to statically typed nature of Java) 
* build definitions leverage the regular Java mechanisms (Inheritance, composition, jar module dependency) to **re-use build elements** or share settings

## See it !

You just need to add such a class in your project in order to make it buildable by Jerkar. To build a project, just execute `jerkar` in a shell at its root folder. 

```java
class TaskBuild extends JkRun {
	
    @JkDoc("Run test in a forked process if true.")
    boolean forkTest;
    
    private JkPathTree src = getBaseTree().goTo("src");
    private Path classDir = getOutputDir().resolve("classes");
    private Path jarFile = getOutputDir().resolve("capitalizer.jar");
    private JkClasspath classpath = JkClasspath.of(getBaseTree()
        .andMatching("libs/compile/*.jar").getFiles());
    private Path testSrc = getBaseDir().resolve("test");
    private Path testClassDir = getOutputDir().resolve("test-classes");
    private JkClasspath testClasspath = classpath.and(getBaseTree()
        .andMatching("libs/test/*.jar").getFiles());
    private Path reportDir = getOutputDir().resolve("junitRreport");
    
    public void doDefault() {
        clean();
        compile();
        junit();
        jar();
    }
    
    public void compile() {
        JkJavaCompiler.ofJdk().compile(JkJavaCompileSpec.of()
            .setClasspath(classpath)
            .addSources(src)
            .setOutputDir(classDir));
        src.andMatching(false,"**/*.java").copyTo(classDir);  /// copy resources
    }
    
    public void jar() {
        JkManifest.ofEmpty().addMainClass("org.jerkar.samples.RunClass")
            .writeToStandardLocation(classDir);
        JkPathTree.of(classDir).zipTo(jarFile);
    }
    
    private void compileTest() {
        JkJavaCompiler.ofJdk().compile(JkJavaCompileSpec.of()
            .setClasspath(testClasspath)
            .addSources(testSrc)
            .setOutputDir(testClassDir));
        src.andMatching(false,"**/*.java").copyTo(testClassDir);  /// copy test resources
    }
    
    public void junit() {
        compileTest();
        JkUnit.of()
            .withReportDir(reportDir).withReport(JunitReportDetail.FULL)
            .withForking(forkTest)
            .run(testClasspath.and(classDir), JkPathTree.of(testClassDir));
    }
    
    public static void main(String[] args) {
        JkInit.instanceOf(TaskBuild.class, args).doDefault();
    }

}
```
Ant like style to build a Java project. All public no-arg returning void are invokable 
from the command line. Executing `jerkar compile jar` compiles source code and creates a Jar file.

```java
class ClassicBuild extends JkRun {

    JkPluginJava javaPlugin = getPlugin(JkPluginJava.class);

    @Override
    protected void setup() {
        JkJavaProject project = javaPlugin.getProject();
        project.setVersionedModule("org.jerkar:examples-java-template", "1.0");
        project.getCompileSpec().setSourceAndTargetVersion(JkJavaVersion.V8);
        project.addDependencies(JkDependencySet.of()
                .and("com.google.guava:guava:18.0")
                .and("junit:junit::4.12"));
    }

    public static void main(String[] args) {
        JkInit.instanceOf(ClassicBuild.class, args).javaPlugin.clean().pack();
    }

}
```

Java plugin helps to build Java project with mi minimal typing. 
Executing `jerkar java#pack java#publish` invokes `pack`and `publish` methods on the java plugin.
<br/>

Jerkar also allows to activate plugins on the fly without explicly instantiating it in ne build class : 
`jerkar sonar# jacoco# java#pack` processes test coverage along SonarQube analysis prior publishing artifacts. 


## Multi-techno projects

Beside **building Java projects**, Jerkar can be used for **any automation purpose**, for example, [Jerkar is used](https://github.com/jerkar/jerkar.github.io/blob/master/_jbake-site-sources/build/def/jerkar/github/io/SiteBuild.java) to generate this site.

For building **multi-techno** projects embedding other technologies than java, we suggest the following approach : 

* Each **sub-project** builds using its **own 'native' tool** (e.g. *nodejs/Webpack* for web-client, *Jerkar* for java server and *Haskell Cabal* for *Haskell* modules)
* **Jerkar** performs the **master build** by **orchestrating sub-builds** and glues all together to pack the whole distribution. 

## Library

Jerkar can also be **embedded** in your product as a simple jar library, to leverage directly the fluent API for manipulating files, launch external tools or other. It is available on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.jerkar%22%20AND%20a%3A%22core%22). 

Icing on the cake : Jerkar has **zero dependency**.


