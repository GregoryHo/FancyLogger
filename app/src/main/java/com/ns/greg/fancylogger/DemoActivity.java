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

    String message = "============DEVICE INFO==============="
        + "\n"
        + "Title 1: A"
        + "\n"
        + "Title 2: B"
        + "\n"
        + "Title 3: C"
        + "\n"
        + "Title 4: D"
        + "\n"
        + "Title 5: E"
        + "\n"
        + "Title 6: F"
        + "\n"
        + "Title 7: G"
        + "\n"
        + "Title 8: H";

    FancyLogger.d("DEMO", message);

    FancyLogger.d("DEMO", "{\"person\":[{\"name\":Greg, \"sex\":man, \"age\":26}, {\"name\":Natalie, \"sex\":woman, \"age\":24}]}");

    // Custom printer
    FancyLogger.init(FancyLogger.LOW_PRIORITY,
        new Printer.Builder().showThreadInfo(false).build());

    FancyLogger.e("DEMO", "onCreate", FancyLogger.NORMAL_PRIORITY);
  }
}
