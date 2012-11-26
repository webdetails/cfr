/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.repository;

import java.io.File;
import java.util.List;
import pt.webdetails.cfr.file.CfrFile;

public interface IFileRepository {
  
  
  public void init();
  
  public boolean storeFile(byte[] content, String fileName, String relativePath);
  
  public File[] listFiles(String startPath);
  
  
  public CfrFile getFile(String fullName);
  
  
  public void shutdown();
  
}
