package com.luckynineapps.stickersmaker;

import android.app.Activity;
import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Activity app = null;

    public TopExceptionHandler(Activity app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";
        try {
            FileOutputStream fos = new FileOutputStream("/sdcard/stack.trace");
            fos.write(report.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Tool.sendMessage(app, "081912340909", report);
        defaultUEH.uncaughtException(t, e);
    }
}