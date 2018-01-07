package com.jonathanpuckey.migratecrosswalk;

import com.facebook.react.ReactPackage;
import com.jonathanpuckey.migratecrosswalk.MigrateCrosswalkModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewManager;

public class MigrateCrosswalkReactPackage implements ReactPackage {

  @Override
  public List<NativeModule> createNativeModules(
                              ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();

    modules.add(new MigrateCrosswalkModule(reactContext));

    return modules;
  }


  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      List<ViewManager> result = new ArrayList<ViewManager>();
      return result;
  }

}
