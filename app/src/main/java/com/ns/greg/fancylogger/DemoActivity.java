package com.ns.greg.fancylogger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ns.greg.library.fancy_logger.FancyLogger;
import com.ns.greg.library.fancy_logger.Printer;

/**
 * @author Gregory
 * @since 2017/7/21
 */

public class DemoActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Using default way
    FancyLogger.add(FancyLogger.LOW_PRIORITY, new Printer.Builder().build());
    // Custom printer
    FancyLogger.add(FancyLogger.LOW_PRIORITY, new Printer.Builder().setMethodOffset(2)
        .showThreadInfo(true)
        .log2File(getApplicationContext(), "Demo_")
        .build());
    // Normal message
    FancyLogger.i("DEMO", "onCreate", FancyLogger.HIGH_PRIORITY);
    // Json
    new Thread(new Runnable() {
      @Override public void run() {
        FancyLogger.d("DEMO",
            "{\"person\":[{\"name\":Greg, \"sex\":man, \"age\":26}, {\"name\":Natalie, \"sex\":woman, \"age\":24}]}");
      }
    }).start();
    // Xml
    new Thread(new Runnable() {
      @Override public void run() {
        FancyLogger.d("DEMO",
            "<param><name value=\"Greg\"/><sex value=\"man\"/><age value=\"26\"/></param>");
      }
    }).start();
    // Long String
    FancyLogger.v("DEMO", "{\n"
        + "  \"a\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\n"
        + "  \"b\": \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\",\n"
        + "  \"c\": \"cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc\"\n"
        + "}");
  }
}
