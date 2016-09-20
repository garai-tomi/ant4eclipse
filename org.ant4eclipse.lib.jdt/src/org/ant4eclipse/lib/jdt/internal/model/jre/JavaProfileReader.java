/**********************************************************************
 * Copyright (c) 2005-2009 ant4eclipse project team.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nils Hartmann, Daniel Kasmeroglu, Gerd Wuetherich
 **********************************************************************/
package org.ant4eclipse.lib.jdt.internal.model.jre;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ant4eclipse.lib.core.Assure;
import org.ant4eclipse.lib.core.Lifecycle;
import org.ant4eclipse.lib.core.service.ServiceRegistryAccess;
import org.ant4eclipse.lib.core.util.StringMap;
import org.ant4eclipse.lib.jdt.model.jre.JavaProfile;

/**
 * <p>
 * Taken from Framework!
 * </p>
 */
public class JavaProfileReader implements Lifecycle {

  /** the java profile cache */
  private Map<String, JavaProfile> _javaProfileCache;

  /** - */
  private JavaProfile              _defaultProfile;

  /**
   * {@inheritDoc}
   */
  public void initialize() {

    this._javaProfileCache = new HashMap<String, JavaProfile>();

    // read all known profiles
    JavaProfile[] javaProfiles = readAllProfiles();

    // add profiles to profile cache
    for (JavaProfile javaProfile : javaProfiles) {
      this._javaProfileCache.put(javaProfile.getName(), javaProfile);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {

  }

  /**
   * {@inheritDoc}
   */
  public boolean isInitialized() {
    return this._javaProfileCache != null;
  }

  /**
   * @return
   */
  public JavaProfile readDefaultProfile() {
    return this._defaultProfile;
  }

  /**
   * Returns a list of all known profiles as string. Mostly intended for debugging purposes
   * 
   * @return string containing all profile names
   */
  public String getAllProfileNames() {
    final List<String> profileNames = new LinkedList<String>(this._javaProfileCache.keySet());
    Collections.sort(profileNames);
    return String.valueOf(profileNames);
  }

  /**
   * {@inheritDoc}
   */
  public JavaProfile getJavaProfile(String path) {
    Assure.nonEmpty("path", path);
    return this._javaProfileCache.get(path);
  }

  public boolean hasJavaProfile(String path) {
    Assure.nonEmpty("path", path);
    return this._javaProfileCache.containsKey(path);
  }

  /**
   * <p>
   * </p>
   * 
   * @param profileFile
   * @return
   */
  public void registerProfile(File profileFile, String jreId) {
    Assure.exists("profileFile", profileFile);
    Assure.nonEmpty("jreId", jreId);

    StringMap props = new StringMap(profileFile);
    JavaProfileImpl javaProfile = new JavaProfileImpl(props);
    javaProfile.setAssociatedJavaRuntimeId(jreId);

    this._javaProfileCache.put(javaProfile.getName(), javaProfile);
  }

  /**
   * @return
   */
  private JavaProfile[] readAllProfiles() {

    // load the profile listing first
    StringMap properties = new StringMap("/profiles/profile.list");

    String javaProfiles = properties.get("java.profiles");

    String[] profiles = javaProfiles.split(",");

    List<JavaProfileImpl> result = new LinkedList<JavaProfileImpl>();

    for (String profile2 : profiles) {
      String profile = profile2.trim();
      if ((profile != null) && !"".equals(profile)) {
        StringMap props = new StringMap("/profiles/" + profile);
        result.add(new JavaProfileImpl(props));
      }
    }

    // set the default profile
    this._defaultProfile = result.get(0);

    return result.toArray(new JavaProfile[result.size()]);
  }

  /**
   * <p>
   * </p>
   * 
   * @return
   */
  public static JavaProfileReader getInstance() {
    return ServiceRegistryAccess.instance().getService(JavaProfileReader.class);
  }
}