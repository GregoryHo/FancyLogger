package com.ns.greg.fancylogger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ns.greg.library.fancy_logger.FancyLogger;
import com.ns.greg.library.fancy_logger.Printer;

/**
 * Created by Gregory on 2017/7/21.
 */

public class DemoActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Using default way
    FancyLogger.init(FancyLogger.LOW_PRIORITY);

    FancyLogger.i("DEMO", "onCreate", FancyLogger.HIGH_PRIORITY);

    // Custom printer
    FancyLogger.init(FancyLogger.LOW_PRIORITY,
        new Printer.Builder().showThreadInfo(false).setMethodOffset(1).setMethodCount(5).build());

    FancyLogger.e("DEMO", "onCreate", FancyLogger.NORMAL_PRIORITY);
  }
}
