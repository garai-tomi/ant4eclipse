package org.ant4eclipse.jdt.ecj;

import org.ant4eclipse.core.Ant4EclipseConfigurator;
import org.ant4eclipse.core.Assert;
import org.ant4eclipse.core.exception.Ant4EclipseException;
import org.ant4eclipse.core.logging.A4ELogging;
import org.ant4eclipse.core.util.StopWatch;
import org.ant4eclipse.core.util.Utilities;

import org.ant4eclipse.jdt.ant.EcjAdditionalCompilerArguments;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Implements a javac compiler adapter for the eclipse compiler for java (ecj). The usage of the ecj has several
 * advantages, e.g. support of access restrictions, multiple source folders.
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class JDTCompilerAdapter extends DefaultCompilerAdapter {

  /** the compiler argument separator */
  private static final String COMPILER_ARGS_SEPARATOR = "=";

  /** the refid key for the additional compiler arguments */
  private static final String COMPILER_ARGS_REFID_KEY = "compiler.args.refid";

  /** - */
  private static final String COMPILER_OPTIONS_FILE   = "compiler.options.file";

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings( { "deprecation", "unchecked" })
  public boolean execute() {

    // Step 1: check preconditions
    preconditions();

    // Step 2: Configure ant4eclipse
    Ant4EclipseConfigurator.configureAnt4Eclipse(getProject());

    // Step 3: Fetch compiler arguments
    EcjAdditionalCompilerArguments ecjAdditionalCompilerArguments = fetchEcjAdditionalCompilerArguments();

    // Step 4: Create the EcjAdapter
    final EcjAdapter ejcAdapter = EcjAdapter.Factory.create();

    // Step 5: create CompileJobDescription

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    final DefaultCompileJobDescription compileJobDescription = new DefaultCompileJobDescription();
    compileJobDescription.setSourceFiles(getSourceFilesToCompile(ecjAdditionalCompilerArguments));
    compileJobDescription.setCompilerOptions(getCompilerOptions());
    compileJobDescription.setClassFileLoader(createClassFileLoader(ecjAdditionalCompilerArguments));

    System.err.println("Setup: " + stopWatch.getElapsedTime());

    // Step 6: Compile
    final CompileJobResult compileJobResult = ejcAdapter.compile(compileJobDescription);

    System.err.println("Compile: " + stopWatch.getElapsedTime());

    // Step 7: dump result
    compileJobResult.dumpProblems();

    // throw Exception if compilation was not successful
    if (!compileJobResult.succeeded()) {
      throw new Ant4EclipseException(EcjExceptionCodes.COMPILATION_WAS_NOT_SUCCESFUL);
    }

    // Step 8: Return
    return true;
  }

  /**
   * <p>
   * Checks the preconditions of the JDTCompilerAdapter
   * </p>
   * 
   * @throws BuildException
   */
  private void preconditions() throws BuildException {

    // source path is not supported!
    if (getJavac().getSourcepath() != null) {
      // TODO: NLS
      throw new BuildException("getJavac().getSourcepath() != null");
    }
  }

  /**
   * <p>
   * Creates the compiler options for the JDT compiler.
   * </p>
   * <p>
   * The compiler options are defined here:
   * <ul>
   * <li><a href="http://help.eclipse.org/galileo/topic/org.eclipse.jdt.doc.isv/guide/jdt_api_options.htm">JDT Core
   * options</a></li>
   * <li>
   * <a href=
   * "http://help.eclipse.org/galileo/topic/org.eclipse.jdt.doc.user/reference/preferences/java/ref-preferences-compiler.htm"
   * >Java Compiler Preferences </a></li>
   * <li>
   * <a href="http://help.eclipse.org/galileo/topic/org.eclipse.jdt.doc.user/reference/preferences/java/compiler/ref-preferences-errors-warnings.htm"
   * >Java Compiler Errors/Warnings Preferences</a></li>
   * </ul>
   * </p>
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  private Map getCompilerOptions() {

    // Step 1: create result
    CompilerOptions compilerOptions = null;

    // 
    String compilerOptionsFileName = extractJavacCompilerArg(COMPILER_OPTIONS_FILE, null);
    if (compilerOptionsFileName != null) {
      File compilerOptionsFile = new File(compilerOptionsFileName);
      if (compilerOptionsFile.exists() && compilerOptionsFile.isFile()) {
        Map<String, String> compilerOptionsMap = Utilities.readProperties(compilerOptionsFile);
        compilerOptions = new CompilerOptions(compilerOptionsMap);
      }
    }

    // create default
    if (compilerOptions == null) {

      // get the source option
      getJavac().getSource();
      // get the target option
      getJavac().getTarget();

      // create compiler options
      compilerOptions = new CompilerOptions();
      // see: http://help.eclipse.org/galileo/topic/org.eclipse.jdt.doc.isv/guide/jdt_api_options.htm#compatibility
      CompilerOptions.versionToJdkLevel("1.5");
      compilerOptions.complianceLevel = ClassFileConstants.JDK1_5;
      compilerOptions.sourceLevel = ClassFileConstants.JDK1_5;
      compilerOptions.targetJDK = ClassFileConstants.JDK1_5;
    }

    System.err.println(compilerOptions.toString());

    // return the compiler options
    return compilerOptions.getMap();
  }

  /**
   * <p>
   * Returns an array with all the source files to compile.
   * </p>
   * 
   * @param compilerArguments
   *          can be null
   * @return the source files to compile
   */
  private SourceFile[] getSourceFilesToCompile(EcjAdditionalCompilerArguments compilerArguments) {

    // get default destination folder
    File defaultDestinationFolder = getJavac().getDestdir();

    // get the files to compile
    final List<SourceFile> sourceFiles = new LinkedList<SourceFile>();

    // iterate over all the source files and create SourceFile
    for (final File file : getJavac().getFileList()) {

      // get the source folder
      final File sourceFolder = getSourceFolder(file);

      // get the relative source file name
      final String sourceFileName = file.getAbsolutePath().substring(
          sourceFolder.getAbsolutePath().length() + File.separator.length());

      // get the destination folder
      File destinationFolder = compilerArguments != null ? compilerArguments.getOutputFolder(sourceFolder)
          : defaultDestinationFolder;

      // add the new source file
      sourceFiles.add(new SourceFile(sourceFolder, sourceFileName, destinationFolder));
    }

    // return the result
    return sourceFiles.toArray(new SourceFile[0]);
  }

  /**
   * <p>
   * Returns the source folder for the given source file.
   * </p>
   * 
   * @param sourceFile
   *          the source file.
   * @return the source folder
   */
  private File getSourceFolder(final File sourceFile) {

    // get the absoult path
    final String absolutePath = sourceFile.getAbsolutePath();

    // get the list of all source directories
    final String[] srcDirs = getJavac().getSrcdir().list();

    // find the 'right' source directory
    for (final String srcDir : srcDirs) {
      if (absolutePath.startsWith(srcDir) && absolutePath.charAt(srcDir.length()) == File.separatorChar) {
        return new File(srcDir);
      }
    }

    // TODO: NLS
    throw new RuntimeException();
  }

  /**
   * <p>
   * Creates class file loader.
   * </p>
   * 
   * @param compilerArguments
   *          the compiler arguments, can be <code>null</code>.
   * @return the class file loader.
   */
  @SuppressWarnings("unchecked")
  private ClassFileLoader createClassFileLoader(final EcjAdditionalCompilerArguments compilerArguments) {

    // Step 1: create class file loader list
    final List<ClassFileLoader> classFileLoaderList = new LinkedList<ClassFileLoader>();

    // Step 2: add boot class loader
    classFileLoaderList.add(createBootClassLoader(compilerArguments));

    // Step 3: add class loader for class path entries
    for (final Iterator iterator = getJavac().getClasspath().iterator(); iterator.hasNext();) {

      // get the file resource
      final FileResource fileResource = (FileResource) iterator.next();

      if (fileResource.getFile().exists()) {

        // TODO: LIBRARY AND PROJECT
        // create class file loader for file resource
        final ClassFileLoader myclassFileLoader = ClassFileLoaderFactory.createClasspathClassFileLoader(fileResource
            .getFile(), EcjAdapter.LIBRARY);

        // create and add FilteringClassFileLoader is necessary
        if (compilerArguments != null && compilerArguments.hasAccessRestrictions(fileResource.getFile())) {
          classFileLoaderList.add(ClassFileLoaderFactory.createFilteringClassFileLoader(myclassFileLoader,
              compilerArguments.getAccessRestrictions(fileResource.getFile())));
        }
        // else add class file loader
        else {
          classFileLoaderList.add(myclassFileLoader);
        }
      }
    }

    // Step 4: return the compound class file loader
    return ClassFileLoaderFactory.createCompoundClassFileLoader(classFileLoaderList.toArray(new ClassFileLoader[0]));
  }

  /**
   * <p>
   * Create a boot class loader.
   * </p>
   * 
   * @param compilerArguments
   *          the compiler arguments , can be <code>null</code>.
   * @return the boot class loader
   */
  @SuppressWarnings("unchecked")
  private ClassFileLoader createBootClassLoader(final EcjAdditionalCompilerArguments compilerArguments) {

    // Step 1: get the boot class path as specified in the javac task
    final Path bootclasspath = getJavac().getBootclasspath();

    // Step 2: create ClassFileLoaders for each entry in the boot class path
    final List<ClassFileLoader> bootClassFileLoaders = new LinkedList<ClassFileLoader>();

    // Step 3: iterate over the boot class path entries as specified in the ant path
    for (final Iterator<FileResource> iterator = bootclasspath.iterator(); iterator.hasNext();) {

      // get the file resource
      final FileResource fileResource = iterator.next();

      // create class file loader
      if (fileResource.getFile().exists()) {
        final ClassFileLoader classFileLoader = ClassFileLoaderFactory.createClasspathClassFileLoader(fileResource
            .getFile(), EcjAdapter.LIBRARY);
        bootClassFileLoaders.add(classFileLoader);
      }
    }

    // Step 4: create compound class file loader
    final ClassFileLoader classFileLoader = ClassFileLoaderFactory.createCompoundClassFileLoader(bootClassFileLoaders
        .toArray(new ClassFileLoader[0]));

    // Step 5: create FilteringClassFileLoader is necessary
    if (compilerArguments != null && compilerArguments.hasBootClassPathAccessRestrictions()) {

      // Step 4: debug
      if (A4ELogging.isDebuggingEnabled()) {
        A4ELogging.debug("Boot class path access restrictions: '%s'", compilerArguments
            .getBootClassPathAccessRestrictions());
      }

      return ClassFileLoaderFactory.createFilteringClassFileLoader(classFileLoader, compilerArguments
          .getBootClassPathAccessRestrictions());
    }
    // else return compound class file loader
    else {
      return classFileLoader;
    }
  }

  /**
   * <p>
   * Helper method that reads the compiler argument with the specified name from the ant's javac task.
   * </p>
   * <p>
   * Compiler arguments can be specified using <code>&lt;compilerarg/&gt;</code> subelement:
   * 
   * <pre>
   * &lt;code&gt; &lt;javac destdir=&quot;${executeJdtProject.default.output.directory}&quot;
   *   debug=&quot;on&quot;
   *   source=&quot;1.5&quot;&gt;
   *   
   *   ...
   * 
   *   &lt;compilerarg value=&quot;compiler.args.refid=executeJdtProject.compiler.args&quot;
   *                compiler=&quot;org.ant4eclipse.jdt.ecj.JDTCompilerAdapter&quot; /&gt;
   * &lt;/javac&gt;
   * &lt;/code&gt;
   * </pre>
   * 
   * </p>
   * 
   * @param argumentName
   * @param defaultValue
   * @return
   */
  private String extractJavacCompilerArg(String argumentName, String defaultValue) {
    Assert.notNull(argumentName);

    // Step 1: Get all compilerArguments
    final String[] currentCompilerArgs = getJavac().getCurrentCompilerArgs();

    // Step 2: Find the 'right' one
    for (final String compilerArg : currentCompilerArgs) {

      // split the argument
      final String[] args = compilerArg.split(COMPILER_ARGS_SEPARATOR);

      // requested one?
      if (args.length > 1 && argumentName.equalsIgnoreCase(args[0])) {

        // return the argument
        return args[1];
      }
    }

    // Step 3: Return defaultValue
    return defaultValue;
  }

  /**
   * <p>
   * Helper method that fetches the {@link EcjAdditionalCompilerArguments} from the underlying ant project. The
   * {@link EcjAdditionalCompilerArguments} are set when a JDT class path is resolved by ant4eclipse.
   * </p>
   * <p>
   * If no {@link EcjAdditionalCompilerArguments} are set, <code>null</code> will be returned.
   * </p>
   * 
   * @return the {@link EcjAdditionalCompilerArguments}
   */
  private EcjAdditionalCompilerArguments fetchEcjAdditionalCompilerArguments() {

    // Step 1: Fetch the CompilerArgument key
    String compilerArgsRefid = extractJavacCompilerArg(COMPILER_ARGS_REFID_KEY, null);

    // Step 2: Return null, if no EcjAdditionalCompilerArguments are set
    if (compilerArgsRefid == null) {
      return null;
    }

    // Step 3: Fetch the compiler arguments
    EcjAdditionalCompilerArguments compilerArguments = (EcjAdditionalCompilerArguments) getProject().getReference(
        compilerArgsRefid);

    // Step 4: Throw exception if null
    if (compilerArguments == null) {
      throw new Ant4EclipseException(EcjExceptionCodes.NO_ECJ_ADDITIONAL_COMPILER_ARGUMENTS_OBJECT, compilerArgsRefid);
    }

    // Step 5: Return the result
    return compilerArguments;
  }
}
