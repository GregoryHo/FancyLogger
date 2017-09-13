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
    FancyLogger.add(FancyLogger.HIGH_PRIORITY, new Printer.Builder().build());
    // Custom printer
    FancyLogger.add(FancyLogger.LOW_PRIORITY, new Printer.Builder().setMethodOffset(2)
        .showThreadInfo(false)
        .log2File(getApplicationContext(), "Demo_")
        .build());

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

    FancyLogger.d("DEMO", message, FancyLogger.NORMAL_PRIORITY);

    FancyLogger.d("DEMO",
        "{\"person\":[{\"name\":Greg, \"sex\":man, \"age\":26}, {\"name\":Natalie, \"sex\":woman, \"age\":24}]}");

    FancyLogger.e("DEMO", "Fancy Logger log to the file!", FancyLogger.LOW_PRIORITY);
  }
}
