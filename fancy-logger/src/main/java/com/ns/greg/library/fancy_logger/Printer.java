package com.ns.greg.library.fancy_logger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.currentThread;

/**
 * Created by Gregory on 2017/7/21.
 */

public class Printer {

  private static final String LOG_PREFIX = "/FancyLogs";

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
  private static final char SPACE = ' ';
  private static final String DOUBLE_DIVIDER =
      "────────────────────────────────────────────────────────";
  private static final String SINGLE_DIVIDER = "────────────────────────────────────────────────";
  private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
  private static final String NEW_LINE = "\n";
  // Title
  private static final String THREAD_TITLE = "Thread: ";
  private static final String MESSAGE_TITLE = "Message: ";

  private static final int MAX_LINE_LENGTH = MIDDLE_BORDER.length() - 11;
  private static final long MAX_LOG_FILE_SIZE = 15 * 1024 * 1024;

  private final boolean showThreadInfo;
  private final int methodOffset;
  private final int methodCount;
  private final Context context;
  private final String prefix;
  private final long logFileSize;

  private Printer(boolean showThreadInfo, int methodOffset, int methodCount, Context context,
      String prefix, long logFileSize) {
    this.showThreadInfo = showThreadInfo;
    this.methodOffset = methodOffset;
    this.methodCount = methodCount;
    WeakReference<Context> weakReference = new WeakReference<>(context);
    this.context = weakReference.get();
    this.prefix = prefix;
    this.logFileSize = logFileSize;
  }

  void log(int verbose, String tag, String message) {
    if (message == null) {
      message = "NULL";
    }

    String decorate = decorateMessage(message);
    if (context != null) {
      log2Files(decorate);
    } else {
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
  }

  private void log2Files(String decorate) {
    try {
      File direct = new File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
              + LOG_PREFIX);

      if (!direct.exists()) {
        direct.mkdir();
      }

      String fileNameTimeStamp =
          new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
      String logTimeStamp =
          new SimpleDateFormat("E MMM dd yyyy 'at' HH:mm:ss:sss", Locale.getDefault()).format(
              new Date());
      String fileName = prefix + fileNameTimeStamp + ".txt";
      File file = new File(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
              + LOG_PREFIX
              + File.separator
              + fileName);
      file.createNewFile();
      if (file.exists()) {
        OutputStream fileOutputStream;
        if (file.length() > logFileSize) {
          fileOutputStream = new FileOutputStream(file, false);
        } else {
          fileOutputStream = new FileOutputStream(file, true);
        }

        fileOutputStream.write((logTimeStamp + "\n" + decorate).getBytes("UTF-8"));
        fileOutputStream.close();
        // Scan file
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null, null);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error while logging into file : " + e);
    }
  }

  private String decorateMessage(String message) {
    StringBuilder builder = new StringBuilder(256);
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
          .append(SPACE)
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
    // corresponding method count with the current stack may exceeds the stack trace. Trims the count
    int methodCount = this.methodCount;
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
        builder.append(SPACE);
      }

      builder.append(getSimpleClassName(trace[stackIndex].getClassName()))
          .append('.')
          .append(trace[stackIndex].getMethodName())
          .append(SPACE)
          .append('(')
          .append(trace[stackIndex].getFileName())
          .append(':')
          .append(trace[stackIndex].getLineNumber())
          .append(')')
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
    int length = messages.length;
    if (length == 1) {
      builder.append(HORIZONTAL_LINE).append(SPACE).append(MESSAGE_TITLE);
      substringMessage(message, false, builder);
      builder.append(BOTTOM_BORDER).append(NEW_LINE);
    } else if (messages.length > 1) {
      builder.append(HORIZONTAL_LINE).append(SPACE).append(MESSAGE_TITLE).append(NEW_LINE);
      for (String msg : messages) {
        substringMessage(msg, true, builder);
      }

      builder.append(BOTTOM_BORDER).append(NEW_LINE);
    }
  }

  private void substringMessage(String message, boolean hasNewLine, StringBuilder builder) {
    int messageLength = message.length();
    if (messageLength > MAX_LINE_LENGTH) {
      int startIndex = 0;
      int endIndex = MAX_LINE_LENGTH;
      while (endIndex <= messageLength) {
        if (startIndex != 0) {
          builder.append(HORIZONTAL_LINE);
          int subSpace = MESSAGE_TITLE.length();
          for (int s = 0; s <= subSpace; s++) {
            builder.append(SPACE);
          }
        }

        builder.append(message.substring(startIndex, endIndex)).append(NEW_LINE);
        if (endIndex == messageLength) {
          break;
        }

        startIndex = endIndex;
        endIndex =
            endIndex + MAX_LINE_LENGTH < messageLength ? endIndex + MAX_LINE_LENGTH : messageLength;
      }
    } else {
      if (!hasNewLine) {
        builder.append(message).append(NEW_LINE);
      } else {
        builder.append(HORIZONTAL_LINE);
        int subSpace = MESSAGE_TITLE.length();
        for (int s = 0; s <= subSpace; s++) {
          builder.append(SPACE);
        }

        builder.append(message).append(NEW_LINE);
      }
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
      if (!name.equals(Printer.class.getName()) && !name.equals(FancyLogger.class.getName())) {
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
    private int methodOffset = 0;
    private int methodCount = 2;
    private Context context;
    private String prefix = "";
    private long logFileSize = MAX_LOG_FILE_SIZE;

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

    public Builder log2File(Context context, String prefix) {
      this.context = context;
      this.prefix = prefix;
      return this;
    }

    /**
     * Save the log to the file
     *
     * @param context context
     * @param prefix file name prefix
     * @param logFileSize file size (bytes)
     */
    public Builder log2File(Context context, String prefix, long logFileSize) {
      this.context = context;
      this.prefix = prefix;
      this.logFileSize = logFileSize;
      return this;
    }

    public Printer build() {
      return new Printer(showThreadInfo, methodOffset, methodCount, context, prefix, logFileSize);
    }
  }
}
