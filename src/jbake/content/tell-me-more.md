title=Tell me more
date=2016-10-05
type=page
status=published
~~~~~~

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

<div class="container">
	<div class="col-md-6">
<pre><code>
class BuildSampleClassic extends JkJavaBuild {
	
    @Override protected JkDependencies dependencies() {
        return JkDependencies.builder() 
            .on(GUAVA, "18.0")  
            .on(JERSEY_SERVER, "1.19")
            .on("com.orientechnologies:orientdb-client:2.0.8")
            .on(JUNIT, "4.11").scope(TEST)
            .on(MOCKITO_ALL, "1.9.5").scope(TEST).build();
    }
    
    public static void main(String[] args) {
        JkInit.instanceOf(BuildSampleClassic.class, args).doDefault();
    }	
}
</code></pre>
	<legend class="small">No more is necessary for building and publishing your artifact. Group and artifact name are inferred from the project folder name and version is defaulted to 'trunk-SNAPSHOT' unless injected at invocation time. <br/>
	It's possible to launch build class directly within the IDE thanks to the main method.</legend>
	</div>
	<div class="col-md-6">
<pre><code>
class MavenStyleBuild extends JkJavaBuild {
	
    @Override public JkModuleId moduleId() {
        return JkModuleId.of("org.jerkar", "script-samples");
    }

    @Override protected JkVersion version() {
        return JkVersion.name("0.3-SNAPSHOT");
    }

    @Override protected JkDependencies dependencies() {
        return JkDependencies.builder()
            .on(GUAVA, "18.0") 
            .on(JERSEY_SERVER, "1.19")
            .on("com.orientechnologies:orientdb-client:2.0.8")
            .on(JUNIT, "4.11").scope(TEST)
            .on(MOCKITO_ALL, "1.9.5").scope(TEST).build();
    }

}
</code></pre>
	<legend class="small">Same as previous but explicitly define group, artifact and current version.</legend> 
	</div>
	<div class="col-md-6">
<pre><code>
class AntStyleBuild extends JkBuild {
	
    String name = "myProject";
    File src = file("src");
    File buildDir = file("build/output");
    File classDir = new File(buildDir, "classes");
    JkClasspath classpath = JkClasspath.of(baseDir().include("libs/*.jar"));
    File reportDir = new File(buildDir, "junitRreport");
	
    @Override public void doDefault() {
        clean();compile();test();
    }
	
    public void compile() {
        JkJavaCompiler.outputtingIn(classDir).withClasspath(classpath).andSourceDir(src).compile();
        JkFileTree.of(src).exclude("**/*.java").copyTo(classDir);
    }
		
    public void test() {
        JkUnit.of(classpath.and(classDir))
            .withClassesToTest(JkFileTree.of(classDir).include("**/*Test.class"))
            .withReportDir(reportDir).run();
    }

}
</code></pre>
	<legend class="small">This build class does not extend JkJavaBuild. As such it explicitly defines what does the build.</legend> 
	</div>
	<div class="col-md-6">
<pre><code>
</code></pre>
	<legend class="small">If the project embeds all its dependency jar files locally, no build class/configuration is needed at all :-)</legend>
	</div>
</div>

Plugin mechanism allows to add behavior without modifying build classes. `jerkar sonar# jacoco# doPublish` processes test coverage along SonarQube analysis prior publishing artifacts. 


## Multi-techno projects

Beside **building Java projects**, Jerkar can be used for **any automation purpose**, for example, [Jerkar is used](https://github.com/jerkar/jerkar.github.io/blob/master/_jbake-site-sources/build/def/jerkar/github/io/SiteBuild.java) to generate this site.

For building **multi-techno** projects embedding other technologies than java, we suggest the following approach : 

* Each **sub-project** builds using its **own 'native' tool** (e.g. *nodejs/Webpack* for web-client, *Jerkar* for java server and *Haskell Cabal* for *Haskell* modules)
* **Jerkar** performs the **master build** by **orchestrating sub-builds** and glues all together to pack the whole distribution. 

## Library

Jerkar can also be **embedded** in your product as a simple jar library, to leverage directly the fluent API for manipulating files, launch external tools or other. It is available on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.jerkar%22%20AND%20a%3A%22core%22). 

Icing on the cake : Jerkar has **zero dependency**.


