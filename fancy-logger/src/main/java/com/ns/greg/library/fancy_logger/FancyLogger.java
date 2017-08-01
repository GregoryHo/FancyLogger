package com.ns.greg.library.fancy_logger;

/**
 * Created by Gregory on 2017/7/21.
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

  private static int currentPriority = LOW_PRIORITY;

  private static Printer fancyPrinter;

  private FancyLogger() {
    throw new AssertionError("No instance.");
  }

  public static void init(int priority) {
    init(priority, new Printer.Builder().build());
  }

  public static void init(int priority, Printer printer) {
    currentPriority = priority;
    fancyPrinter = printer;
  }

  public static void v(String tag, String message) {
    v(tag, message, HIGH_PRIORITY);
  }

  public static void v(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, VERBOSE);
    }
  }

  public static void d(String tag, String message) {
    d(tag, message, HIGH_PRIORITY);
  }

  public static void d(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, DEBUG);
    }
  }

  public static void i(String tag, String message) {
    i(tag, message, HIGH_PRIORITY);
  }

  public static void i(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, INFO);
    }
  }

  public static void w(String tag, String message) {
    w(tag, message, HIGH_PRIORITY);
  }

  public static void w(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, WARN);
    }
  }

  public static void e(String tag, String message) {
    e(tag, message, HIGH_PRIORITY);
  }

  public static void e(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, ERROR);
    }
  }

  public static void wtf(String tag, String message) {
    wtf(tag, message, HIGH_PRIORITY);
  }

  public static void wtf(String tag, String message, int priority) {
    if (priority <= currentPriority) {
      log(tag, message, WTF);
    }
  }

  private static void log(String tag, String message, int verbose) {
    if (fancyPrinter == null) {
      throw new AssertionError("You should call init() before use it.");
    }

    fancyPrinter.log(verbose, tag, message);
  }
}
