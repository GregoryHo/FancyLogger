package com.ns.greg.library.fancy_logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gregory
 * @since 2017/7/21
 */

public class FancyLogger {

  // Message priority
  public static final int NON_DEBUG_PRIORITY = 0;
  public static final int HIGH_PRIORITY = 1;
  public static final int NORMAL_PRIORITY = 2;
  public static final int LOW_PRIORITY = 3;

  // Message level
  static final int VERBOSE = 2;
  static final int DEBUG = 3;
  static final int INFO = 4;
  static final int WARN = 5;
  static final int ERROR = 6;
  static final int WTF = 7;

  private static Map<Integer, Printer> printers = new ConcurrentHashMap<>();

  private FancyLogger() {
    throw new AssertionError("No instance.");
  }

  public static void add(int priority, Printer printer) {
    printers.put(priority, printer);
  }

  public static void v(String tag, String message) {
    v(tag, message, HIGH_PRIORITY);
  }

  public static void v(String tag, String message, int priority) {
    log(priority, VERBOSE, tag, message);
  }

  public static void d(String tag, String message) {
    d(tag, message, HIGH_PRIORITY);
  }

  public static void d(String tag, String message, int priority) {
    log(priority, DEBUG, tag, message);
  }

  public static void i(String tag, String message) {
    i(tag, message, HIGH_PRIORITY);
  }

  public static void i(String tag, String message, int priority) {
    log(priority, INFO, tag, message);
  }

  public static void w(String tag, String message) {
    w(tag, message, HIGH_PRIORITY);
  }

  public static void w(String tag, String message, int priority) {
    log(priority, WARN, tag, message);
  }

  public static void e(String tag, String message) {
    e(tag, message, HIGH_PRIORITY);
  }

  public static void e(String tag, String message, int priority) {
    log(priority, ERROR, tag, message);
  }

  public static void wtf(String tag, String message) {
    wtf(tag, message, HIGH_PRIORITY);
  }

  public static void wtf(String tag, String message, int priority) {
    log(priority, WTF, tag, message);
  }

  private static void log(int priority, int verbose, String tag, String message) {
    for (int key : printers.keySet()) {
      if (priority <= key) {
        printers.get(key).log(verbose, tag, message);
      }
    }
  }
}
