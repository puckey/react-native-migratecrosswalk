package com.jonathanpuckey.migratecrosswalk;

import android.app.Application;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.io.File;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.*;

public class MigrateCrosswalkModule extends ReactContextBaseJavaModule {
  public static final String TAG = "MigrateCrosswalk";

  private static String XwalkPath = "app_xwalkcore/Default";

  // Root dir for system webview data used by Android 4.4+
  private static String modernWebviewDir = "app_webview";

  // Root dir for system webview data used by Android 4.3 and below
  private static String oldWebviewDir = "app_database";

  // Directory name for local storage files used by Android 4.4+ and XWalk
  private static String modernLocalStorageDir = "Local Storage";

  // Directory name for local storage files used by Android 4.3 and below
  private static String oldLocalStorageDir = "localstorage";

  // Storage directory names used by Android 4.4+ and XWalk
  private static String[] modernAndroidStorage = {
          "Cache",
          "Cookies",
          "Cookies-journal",
          "IndexedDB",
          "databases"
  };

  private boolean isModernAndroid;
  private File appRoot;
  private File XWalkRoot;
  private File webviewRoot;

  public MigrateCrosswalkModule(ReactApplicationContext reactContext) {
		super(reactContext);
    Log.d(TAG, "running Crosswalk migration shim");

    boolean found = lookForXwalk(reactContext.getFilesDir());
    if (!found){
        lookForXwalk(reactContext.getExternalFilesDir(null));
    }

    if(found){
        migrateData(reactContext);
    }
	}

  @Override
  public String getName() {
    return "MigrateCrosswalk";
  }

  private boolean lookForXwalk(File filesPath){
    File root = getStorageRootFromFiles(filesPath);
    boolean found = testFileExists(root, XwalkPath);
    if(found){
        Log.d(TAG, "found Crosswalk directory");
        appRoot = root;
    }else{
        Log.d(TAG, "Crosswalk directory NOT FOUND");
    }
    return found;
  }

  private void migrateData(ReactApplicationContext reactContext){
    isModernAndroid = Build.VERSION.SDK_INT >= 19;
    XWalkRoot = constructFilePaths(appRoot, XwalkPath);

      webviewRoot = constructFilePaths(appRoot, getWebviewPath());

      boolean hasMigratedData = false;

      if(testFileExists(XWalkRoot, modernLocalStorageDir)){
          Log.d(TAG, "Local Storage data found");
          moveDirFromXWalkToWebView(modernLocalStorageDir, getWebviewLocalStoragePath());
          Log.d(TAG, "Moved Local Storage from XWalk to System Webview");
          hasMigratedData = true;
      }

      if(isModernAndroid){
          for(String dirName : modernAndroidStorage){
              if(testFileExists(XWalkRoot, dirName)) {
                  moveDirFromXWalkToWebView(dirName);
                  Log.d(TAG, "Moved " + dirName + " from XWalk to System Webview");
                  hasMigratedData = true;
              }
          }
      }

      if(hasMigratedData){
        deleteRecursive(XWalkRoot);
      }
  }

  private void moveDirFromXWalkToWebView(String dirName){
      File XWalkLocalStorageDir = constructFilePaths(XWalkRoot, dirName);
      File webviewLocalStorageDir = constructFilePaths(webviewRoot, dirName);
      XWalkLocalStorageDir.renameTo(webviewLocalStorageDir);
  }

  private void moveDirFromXWalkToWebView(String sourceDirName, String targetDirName){
      File XWalkLocalStorageDir = constructFilePaths(XWalkRoot, sourceDirName);
      File webviewLocalStorageDir = constructFilePaths(webviewRoot, targetDirName);
      XWalkLocalStorageDir.renameTo(webviewLocalStorageDir);
  }


  private String getWebviewPath(){
      if(isModernAndroid){
          return modernWebviewDir;
      }else{
          return oldWebviewDir;
      }
  }

  private String getWebviewLocalStoragePath(){
      if(isModernAndroid){
          return modernLocalStorageDir;
      }else{
          return oldLocalStorageDir;
      }
  }

  private boolean testFileExists(File root, String name) {
      boolean status = false;
      if (!name.equals("")) {
          File newPath = constructFilePaths(root.toString(), name);
          status = newPath.exists();
          Log.d(TAG, "exists '"+newPath.getAbsolutePath()+": "+status);
      }
      return status;
  }

  private File constructFilePaths (File file1, File file2) {
      return constructFilePaths(file1.getAbsolutePath(), file2.getAbsolutePath());
  }

  private File constructFilePaths (File file1, String file2) {
      return constructFilePaths(file1.getAbsolutePath(), file2);
  }

  private File constructFilePaths (String file1, String file2) {
      File newPath;
      if (file2.startsWith(file1)) {
          newPath = new File(file2);
      }
      else {
          newPath = new File(file1 + "/" + file2);
      }
      return newPath;
  }

  private File getStorageRootFromFiles(File filesDir){
      String filesPath = filesDir.getAbsolutePath();
      filesPath = filesPath.replaceAll("/files", "");
      return new File(filesPath);
  }

  private void deleteRecursive(File fileOrDirectory) {
      if (fileOrDirectory.isDirectory())
          for (File child : fileOrDirectory.listFiles())
              deleteRecursive(child);

      fileOrDirectory.delete();
  }
}
