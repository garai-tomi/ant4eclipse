package org.ant4eclipse.core.ant;

import org.ant4eclipse.core.service.ServiceRegistry;

import org.ant4eclipse.testframework.TestDirectory;
import org.apache.tools.ant.BuildFileTest;

import java.io.File;

/**
 * Base-class for all tests that deals with 'real' build files
 * 
 * <p>
 * The build files are copied from the classpath to a (temp) test directory before executing the test
 * 
 * @author Nils Hartmann (nils@nilshartmann.net)
 */
public abstract class AbstractAnt4EclipseBuildFileTest extends BuildFileTest {

  private TestDirectory _testDirectory;

  public void setUp() throws Exception {
    _testDirectory = new TestDirectory(true);

    File buildFile = _testDirectory.createFile(getBuildFileName(), getResource(getBuildFileName()).openStream());

    configureProject(buildFile.getAbsolutePath());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    _testDirectory.dispose();
    ServiceRegistry.reset();
  }

  /**
   * Returns the build file name.
   * 
   * <p>
   * The name must be relative to this class. The file must be available on classpath
   * 
   * @return
   */
  protected abstract String getBuildFileName();

}
