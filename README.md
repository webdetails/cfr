![image](http://www.webdetails.pt/images/wd_pentaho_horizontal.png) 


# Community File Repository (CFR)


## Overview

CFR (Community File Repository) is a new CTool made by
[Webdetails](http://www.webdetails.pt) that allows you to setup and use a file
repository from inside or outside Pentaho. You can use it to manage your file
needs, allowing uploads and downloads from inside your dashboards. 

### Motivation

Frequently we come across several file management needs in the context of the
Pentaho platform:

- A user that wishes to let the users browse and download some documents when
  viewing a dashboard;
- An easy way for a user to upload some files to the BA Server (maybe to be used
  by an ETL job);
- The ability to explore, create and delete items in the Solution Repository
  when in a dashboard.

As always, when we are faced with the same kind of task more than twice, we
start to wonder whether we couldn't build a CTool that could help us (and the
community) automating that task. In this scenario, we came up with CFR -
Community File Repository.


### Requirements and Compatibility

This plugin was built and tested against _Pentaho 5.3_, but should be compatible
with earlier releases up to _Pentaho 4.8_.

As most of the other ctools, it also requires
[CDE](http://www.webdetails.pt/ctools/cde.html),
[CDA](http://www.webdetails.pt/ctools/cda.html) and
[CDF](http://www.webdetails.pt/ctools/cdf.html). Make sure that you have updated
versions of these plugins when you install CFR.



### Main features

- Ability to upload and download files from the repository
- Ability to create folders and delete items from the repository
- Provides an audit database which lets you track who changed the repository and
  when.
- Provides a FileBrowser component for CDE that lets you browse the repository
- Provides a FileUploader component for CDE that lets you upload files to the
  repository
- Two distinct repositories are available (just change a configuration setting):
	- Pentaho Solution Repository (whether DB or File based)
	- Filesystem Based repository (based on a folder in your BA Server machine
	  or some other accessible machine)	


## Install & Config

This is a Pentaho plugin, so you can install it as as you would do it with any
other plugin (unzip the contents into your pentaho-solutions/system folder). It
will soon be available to download and install from within the _Pentaho
Marketplace_


### Build and install the plugin 

Here are the instructions if you want to manually build the plugin:


- Clone public Git repository available [here](the
  https://github.com/webdetails/cfr.git)
- To build the plugin, you can run use the command line: `ant dist-plugin`. This
  will create a .zip file inside the `/dist` folder
- Unzip the file and copy the entire cfr folder to your `system` folder, located
  inside your `pentaho-solutions` folder 


### Settings 

Inside your plugin folder there is a file named `cfr.spring.xml` where you can
define how the plugin's behaviour.  

Files and folders are by default stored in `system/.cfr`. You can also configure
some other root folder by editing the configuration file `cfr.spring.xml` and
adding an attribute for your file repository with the full name of the starting
folder.

<property name="basePath">
    <value>
        path/to/cfr/uploads
    </value>
</property

You can define another class, changing the value of `repositoryClass`. Right
now, you can choose
`pt.webdetails.cfr.repository.PentahoRepositoryFileRepository` for the Pentaho
Solution Repository or `pt.webdetails.cfr.repository.DefaultFileRepository` for
a file system based repository.

<bean id="IFileRepository" class="pt.webdetails.cfr.repository.DefaultFileRepository" scope="prototype"/>

You can roll up your own version of a repository or develop a wrapper to
whatever you’re using to manage your files. Just implement the
`pt.webdetails.cfr.repository.IFileRepository`, drop the resulting class or jar
in the `system/cfr/lib` folder and reference the class from `cfr.spring.xml`.

	<repositoryClass>
		pt.webdetails.cfr.repository.IFileRepository
	</repositoryClass>


## Working with the plugin

The plugin can be accessed via a user interface UI or using web requests. 

### User Interface 

You can manage the plugin via a friendly user interface

**User interface URL:**
`http://<pentaho-ba-server-host>:<port>/pentaho/plugin/cfr/api/home`

It will be presented a page where you can upload or download files (choosing a
folder outside the pentaho repository allows you to manage the files with
traditional file operations "copy/move")  


### Web Requests


#### Files and folders operations 

To create, delete, list or get files and folders there are some exposed
interfaces you can use.  

###### Exposed Interfaces 

_Base URL:_ `http://<pentaho-ba-server-host>:<port>/pentaho/plugin/cfr/api/`

* */createFolder* --> creates a folder

	* Query Parameters:**
		* path --> specifies the folder we want to create
	*Examples:*
		* `/createFolder?path=/folder/subfolder` --> creates the specified
		  folder

- */remove* --> removes a file from a folder

	* Query Parameters:
		* fileName --> specifies the file or folder to delete 
	* Examples:
		* `/remove?path=/folder/subfolder/file.txt` --> removes the file with
		  name "file.txt" located inside "subfolder"
		* `/remove?path=/folder/subfolde`r --> removes folder with name
		  _subfolder_

- */listFilesJSON* --> returns a [Json](http://www.json.org/) object with all
  the files and folder. Gets the root folder if no parameter is specified 

	* Query Parameters:
		* dir --> (optional) specifies which folder we want to list content from
	* Examples:
		* `/listFilesJSON` --> returns a [Json](http://www.json.org/) object
		  with all files and folders in the root dir
		* `/listFilesJSON?dir=/folder/subfolder` --> returns a
		  [Json](http://www.json.org/) object with all the file elements inside
		  the specified folder

- */createFolder* --> creates a folder

	* Query Parameters:
		* path --> specifies the folder we want to create
	* Examples:
		* _/createFolder?path=/folder/subfolder_ --> creates the specified
		  folder
	
- */getFile* --> returns the specified file only if the user has read
  permissions on it

	* Query Parameters:
		* fileName --> specifies the file we want to retrieve, with the path
		  relative to the repository root
    
	* Examples:    
		* `/getFile?fileName=/example.txt` -> retrieves the example.txt file
		  stored in the root folder of the repository

- */viewFile* --> if called in a browser and supported by the browser itself the
  specified file is previewed in it, but only if the user has read permissions
  on it

	* Query Parameters:
		* fileName --> specifies the file we want to preview/retrieve, with the
		  path relative to the repository root
	* Examples:
		* `/viewFile?fileName=/example.txt` -> retrieves the example.txt file
		  stored in the root folder and it is called in a browser. The file is
		  rendered directly inside the browser (if supported)


#### Permissions management

The access to the files repository is granted by default to the files owner and
to all the user administrators. If granting access to files to other users is
needed it must be set using the interfaces exposed below.


###### Exposed Interfaces 

** Base URL: ** `http://<pentaho-ba-server-host>:<port>/pentaho/content/cfr` for Pentaho 4.x
** `http://<pentaho-ba-server-host>:<port>/pentaho/plugin/cfr/api` for Pentaho 5.0


- */setPermissions* --> 	Setting permissions is only allowed to the folder/file owner to which we want to set permissions or to any system administrator

	* Query Parameters:
		* _path_ --> specifies the folder or file in which we want to set the
		  permissions
		* _id_ --> specifies the role or username to which we want to grant the
		  permissions
		* _permission_ --> this is an optional parameter, by default it assumes
		  the "read only" value. It is used to specify the permissions to be
		  granted [for now only the "read" permissions are handled]
	
	* Examples:
		* _/setPermissions?path=/test/&id=suzy_ --> sets read permissions for
		  user suzy on repository folder test/ [this allows access to all the
		  files contained in the folder]
		* _/setPermissions?path=test.txt&id=joe_ --> sets read permissions for
		  user joe on file test.txt
		* _/setPermissions?path=/&id=suzy&permission=read&permission=write_ -->
		  sets read and write permissions for user suzy on repository root
		  folder [this functionality isn't yet totally implemented]

- */getPermissions*
	
	* Query Parameters:
		* _path_ --> specifies the folder or file in which we want to check the
		  permissions
		* _id_ --> specifies the role or username to which we want to check the
		  permissions

- * /deletePermissions * --> deleting permissions is only allowed to the
  folder/file owner or to any system administrator
	* Query Parameters:
		* _path_ --> specifies the folder or file in which we want to revoke
		  permissions
		* _id_ --> specifies the role or username to which we want to revoke the
		  permissions
	* Examples:
		* /deletePermissions?path=test.txt&id=suzy --> revokes permission to
		  file test.txt to user suzy

##FAQ

0. *How do I change the type of repository ?* 
	* Edit settings.xml and change the value of repositoryClass. Right now, you
	  can choose pt.webdetails.cfr.repository.PentahoRepositoryFileRepository
	  for the Pentaho Solution Repository or
	  pt.webdetails.cfr.repository.DefaultFileRepository for a file system based
	  repository.

0. *How can I use another repository ?*
	* You can roll up your own version of a repository or develop a wrapper to
	  whatever you’re using to manage your files. Just implement the
	  pt.webdetails.cfr.repository.IFileRepository, drop the resulting class or
	  jar in the system/cfr/lib folder and reference the class from
	  settings.xml.

0. *Where are the files stored in the DefaultFileRepository?*
	* They are stored in system/.cfr. You can also configure some other root
	  folder by editing settings.xml and adding an attribute
	  defaultFileRepositoryRoot with the full name of the starting folder for
	  your repository.

0. *Can I upload a file over a web request?*
	* Yes, we can build and execute a POST request specifying the correct
	  options. For more details on this, please contact
	  [Webdetails](http://www.webdetails.pt/) 

