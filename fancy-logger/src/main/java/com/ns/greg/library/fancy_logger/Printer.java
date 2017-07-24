package com.ns.greg.library.fancy_logger;

import android.util.Log;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.currentThread;

/**
 * Created by Gregory on 2017/7/21.
 */

public class Printer {

  /**
   * @see <a href="http://stackoverflow.com/a/8899735" />
   */
  private static final int ENTRY_MAX_LEN = 4000;

  // The minimum stack trace index, starts at this class after two native calls.
  private static final int MIN_STACK_OFFSET = 2;

  private static final int JSON_INDENT = 2;

  // Message box
  private static final char TOP_LEFT_CORNER = '┌';
  private static final char BOTTOM_LEFT_CORNER = '└';
  private static final char MIDDLE_CORNER = '├';
  private static final char HORIZONTAL_LINE = '│';
  private static final String DOUBLE_DIVIDER =
      "────────────────────────────────────────────────────────";
  private static final String SINGLE_DIVIDER =
      "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
  private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
  private static final String NEW_LINE = "\n";

  // Title
  private static final String THREAD_TITLE = " Thread: ";
  private static final String MESSAGE_TITLE = " Message: ";

  private boolean showThreadInfo;
  private int methodOffset;
  private int methodCount;

  private Printer(boolean showThreadInfo, int methodOffset, int methodCount) {
    this.showThreadInfo = showThreadInfo;
    this.methodOffset = methodOffset;
    this.methodCount = methodCount;
  }

  void log(int verbose, String tag, String message) {
    String decorate = decorateMessage(message);

    switch (verbose) {
      case FancyLogger.VERBOSE:
        Log.v(tag, decorate);
        break;

      case FancyLogger.DEBUG:
        Log.d(tag, decorate);
        break;

      case FancyLogger.INFO:
        Log.i(tag, decorate);
        break;

      case FancyLogger.WARN:
        Log.w(tag, decorate);
        break;

      case FancyLogger.ERROR:
        Log.e(tag, decorate);
        break;

      case FancyLogger.WTF:
        Log.w(tag, decorate);
        break;

      default:
        break;
    }
  }

  private String decorateMessage(String message) {
    StringBuilder builder = new StringBuilder();
    logHeader(builder);
    logMethod(builder);
    logMessage(message, builder);

    return builder.toString();
  }

  private void logHeader(StringBuilder builder) {
    if (showThreadInfo) {
      builder.append(TOP_BORDER)
          .append(NEW_LINE)
          .append(HORIZONTAL_LINE)
          .append(THREAD_TITLE)
          .append(currentThread().getName())
          .append(NEW_LINE);
    } else {
      builder.append(TOP_BORDER).append(NEW_LINE);
    }
  }

  private void logMethod(StringBuilder builder) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int stackOffset = getStackOffset(trace) + methodOffset;
    //corresponding method count with the current stack may exceeds the stack trace. Trims the count
    if (methodCount + stackOffset > trace.length) {
      methodCount = trace.length - stackOffset - 1;
    }

    if (methodCount > 0 && showThreadInfo) {
      builder.append(MIDDLE_BORDER).append(NEW_LINE);
    }

    int space = 1;
    for (int i = methodCount; i > 0; i--) {
      int stackIndex = i + stackOffset;
      if (stackIndex >= trace.length) {
        continue;
      }

      builder.append(HORIZONTAL_LINE);
      for (int s = 0; s < space; s++) {
        builder.append(' ');
      }

      builder.append(getSimpleClassName(trace[stackIndex].getClassName()))
          .append(".")
          .append(trace[stackIndex].getMethodName())
          .append(" ")
          .append(" (")
          .append(trace[stackIndex].getFileName())
          .append(":")
          .append(trace[stackIndex].getLineNumber())
          .append(")")
          .append(NEW_LINE);

      space += 2;
    }

    if (methodCount > 0) {
      builder.append(MIDDLE_BORDER).append(NEW_LINE);
    }
  }

  private void logMessage(String message, StringBuilder builder) {
    // Convert JSON
    if (message.startsWith("{")) {
      try {
        JSONObject jsonObject = new JSONObject(message);
        message = jsonObject.toString(JSON_INDENT);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } else if (message.startsWith("[")) {
      try {
        JSONArray jsonArray = new JSONArray(message);
        message = jsonArray.toString(JSON_INDENT);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    String[] messages = message.split(NEW_LINE);
    if (messages.length == 1) {
      builder.append(HORIZONTAL_LINE)
          .append(MESSAGE_TITLE)
          .append(message)
          .append(NEW_LINE)
          .append(BOTTOM_BORDER);
    } else {
      builder.append(HORIZONTAL_LINE).append(MESSAGE_TITLE).append(NEW_LINE);
      for (String msg : messages) {
        builder.append(HORIZONTAL_LINE);
        int subSpace = MESSAGE_TITLE.length() - 1;
        for (int s = 0; s < subSpace; s++) {
          builder.append(' ');
        }

        builder.append(msg).append(NEW_LINE);
      }

      builder.append(BOTTOM_BORDER);
    }
  }

  /**
   * Determines the starting index of the stack trace, after method calls made by this class.
   *
   * @param trace the stack trace
   * @return the stack offset
   */
  private int getStackOffset(StackTraceElement[] trace) {
    for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
      StackTraceElement e = trace[i];
      String name = e.getClassName();
      if (!name.equals(Printer.class.getName()) && !name.equals(Logger.class.getName())) {
        return --i;
      }
    }

    return -1;
  }

  private String getSimpleClassName(String name) {
    int lastIndex = name.lastIndexOf(".");
    return name.substring(lastIndex + 1);
  }

  public static final class Builder {

    private boolean showThreadInfo = true;
    private int methodOffset = 1;
    private int methodCount = 2;

    public Builder showThreadInfo(boolean showThreadInfo) {
      this.showThreadInfo = showThreadInfo;
      return this;
    }

    public Builder setMethodOffset(int methodOffset) {
      this.methodOffset = methodOffset;
      return this;
    }

    public Builder setMethodCount(int methodCount) {
      this.methodCount = methodCount;
      return this;
    }

    public Printer build() {
      return new Printer(showThreadInfo, methodOffset, methodCount);
    }
  }
}
