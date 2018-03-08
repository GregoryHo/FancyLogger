package com.ns.greg.library.fancy_logger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.currentThread;

/**
 * @author Gregory
 * @since 2017/7/21
 */

public class Printer {

  private static final String LOG_PREFIX = "/FancyLogs";

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
  private static final long DEFAULT_LOG_FILE_SIZE = 100 * 1_024 * 1_024;

  private final boolean showThreadInfo;
  private final int methodOffset;
  private final int methodCount;
  private final Context context;
  private final String prefix;
  private final long logFileSize;
  private final StringBuilder contentBuilder = new StringBuilder();

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

  /* Avoiding multi-thread competition */ synchronized void log(int verbose, String tag, String message) {
    if (message == null) {
      message = "NULL";
    }

    logHeader(verbose, tag);
    logMethod(verbose, tag);
    logMessage(verbose, tag, message);
  }

  private void logHeader(int verbose, String tag) {
    if (showThreadInfo) {
      contentBuilder.append(TOP_BORDER).append(NEW_LINE);
      logContent(verbose, tag, getContent());
      contentBuilder.append(HORIZONTAL_LINE)
          .append(SPACE)
          .append(THREAD_TITLE)
          .append(currentThread().getName())
          .append(NEW_LINE);
      logContent(verbose, tag, getContent());
    } else {
      contentBuilder.append(TOP_BORDER).append(NEW_LINE);
      logContent(verbose, tag, getContent());
    }
  }

  private void logMethod(int verbose, String tag) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int stackOffset = getStackOffset(trace) + methodOffset;
    // corresponding method count with the current stack may exceeds the stack trace. Trims the count
    int methodCount = this.methodCount;
    if (methodCount + stackOffset > trace.length) {
      methodCount = trace.length - stackOffset - 1;
    }

    if (methodCount > 0 && showThreadInfo) {
      contentBuilder.append(MIDDLE_BORDER).append(NEW_LINE);
      logContent(verbose, tag, getContent());
    }

    int space = 1;
    for (int i = methodCount; i > 0; i--) {
      int stackIndex = i + stackOffset;
      if (stackIndex >= trace.length) {
        continue;
      }

      contentBuilder.append(HORIZONTAL_LINE);
      for (int s = 0; s < space; s++) {
        contentBuilder.append(SPACE);
      }

      contentBuilder.append(getSimpleClassName(trace[stackIndex].getClassName()))
          .append('.')
          .append(trace[stackIndex].getMethodName())
          .append(SPACE)
          .append('(')
          .append(trace[stackIndex].getFileName())
          .append(':')
          .append(trace[stackIndex].getLineNumber())
          .append(')')
          .append(NEW_LINE);
      logContent(verbose, tag, getContent());
      space += 2;
    }

    if (methodCount > 0) {
      contentBuilder.append(MIDDLE_BORDER).append(NEW_LINE);
      logContent(verbose, tag, getContent());
    }
  }

  private void logMessage(int verbose, String tag, String message) {
    contentBuilder.append(HORIZONTAL_LINE).append(SPACE).append(MESSAGE_TITLE).append(NEW_LINE);
    logContent(verbose, tag, getContent());
    // Check JSON
    if (message.startsWith("{")) {
      try {
        JSONObject jsonObject = new JSONObject(message);
        message = jsonObject.toString(JSON_INDENT);
      } catch (JSONException e) {
      }
    } else if (message.startsWith("[")) {
      try {
        JSONArray jsonArray = new JSONArray(message);
        message = jsonArray.toString(JSON_INDENT);
      } catch (JSONException e) {
      }
    }

    // Check XML
    try {
      Source source = new StreamSource(new StringReader(message));
      StreamResult result = new StreamResult(new StringWriter());
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(source, result);
      message = result.getWriter().toString().replaceFirst(">", ">\n");
    } catch (TransformerException e) {
    }

    String[] messages = message.split(NEW_LINE);
    for (String msg : messages) {
      substringMessage(verbose, tag, msg);
    }

    contentBuilder.append(BOTTOM_BORDER).append(NEW_LINE);
    logContent(verbose, tag, getContent());
  }

  private void substringMessage(int verbose, String tag, String message) {
    int messageLength = message.length();
    if (messageLength > MAX_LINE_LENGTH) {
      int startIndex = 0;
      int endIndex = MAX_LINE_LENGTH;
      while (endIndex <= messageLength) {
        logChunk(verbose, tag, message.substring(startIndex, endIndex));
        if (endIndex == messageLength) {
          break;
        }

        startIndex = endIndex;
        endIndex =
            endIndex + MAX_LINE_LENGTH < messageLength ? endIndex + MAX_LINE_LENGTH : messageLength;
      }
    } else {
      logChunk(verbose, tag, message);
    }
  }

  private void logChunk(int verbose, String tag, String message) {
    contentBuilder.append(HORIZONTAL_LINE);
    int subSpace = MESSAGE_TITLE.length();
    for (int s = 0; s <= subSpace; s++) {
      contentBuilder.append(SPACE);
    }

    contentBuilder.append(message).append(NEW_LINE);
    logContent(verbose, tag, getContent());
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

  private String getContent() {
    String content = contentBuilder.toString();
    contentBuilder.setLength(0);
    return content;
  }

  private void logContent(int verbose, String tag, String message) {
    log2Chat(verbose, tag, message);
    log2File(message);
  }

  private void log2Chat(int verbose, String tag, String message) {
    switch (verbose) {
      case FancyLogger.VERBOSE:
        Log.v(tag, message);
        break;

      case FancyLogger.DEBUG:
        Log.d(tag, message);
        break;

      case FancyLogger.INFO:
        Log.i(tag, message);
        break;

      case FancyLogger.WARN:
        Log.w(tag, message);
        break;

      case FancyLogger.ERROR:
        Log.e(tag, message);
        break;

      case FancyLogger.WTF:
        Log.w(tag, message);
        break;

      default:
        break;
    }
  }

  private void log2File(String decorate) {
    if (context == null) {
      // Ignored
      return;
    }

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
        fileOutputStream = new FileOutputStream(file, file.length() < logFileSize);
        fileOutputStream.write((logTimeStamp + ": " + decorate).getBytes("UTF-8"));
        fileOutputStream.close();
        // Scan file
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null, null);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error while logging into file : " + e);
    }
  }

  public static final class Builder {

    private boolean showThreadInfo;
    private int methodOffset;
    private int methodCount;
    private Context context;
    private String prefix;
    private long logFileSize;

    public Builder() {
      this.showThreadInfo = true;
      this.methodOffset = 0;
      this.methodCount = 2;
      this.context = null;
      this.prefix = "";
      this.logFileSize = DEFAULT_LOG_FILE_SIZE;
    }

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

    /**
     * Saves the log to the file
     *
     * @param context context
     * @param prefix file name prefix
     */
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
