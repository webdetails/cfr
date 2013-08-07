/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cfr.repository;

import pt.webdetails.cfr.file.CfrFile;
import pt.webdetails.cfr.file.IFile;

public interface IFileRepository {

  public void init();

  public boolean storeFile(byte[] content, String fileName, String relativePath);

  public IFile[] listFiles(String startPath);

  public CfrFile getFile(String fullName);

  public boolean createFolder(String fullPathName);

  public boolean deleteFile(String fullName);

  public void shutdown();

}
