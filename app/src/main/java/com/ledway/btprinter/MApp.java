package com.ledway.btprinter;

import android.app.Application;

/**
 * Created by togb on 2016/5/21.
 */
public class MApp extends Application {
  private static MApp instance ;

  @Override public void onCreate() {
    super.onCreate();
    instance = this;
  }
  public static MApp getInstance(){
    return instance;
  }

}
