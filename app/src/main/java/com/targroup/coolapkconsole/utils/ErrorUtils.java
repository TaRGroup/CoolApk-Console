package com.targroup.coolapkconsole.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Trumeet on 2017/5/1.
 * @author Trumeet
 * @since 1.32
 */

public class ErrorUtils {
    private static final String TAG = ErrorUtils.class.getSimpleName();

    private static boolean checkIsIOException (@NonNull Throwable e) {
        if (e instanceof IOException) return true;
        Throwable throwable = e.getCause();
        if (throwable != null) {
            return checkIsIOException(throwable);
        }
        return false;
    }

    /**
     * Show a error dialog
     * @param e throwable
     */
    public static AlertDialog showErrorDialog (@NonNull Throwable e, @NonNull final
    Context context) {
        boolean isNetworkException = checkIsIOException(e);
        final String message = isNetworkException ? "Network Error" :
                convertThrowable(e);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("ERROR")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false);
        if (!isNetworkException) {
            builder.setNegativeButton(android.R.string.copy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE))
                            .setText(message);
                    Toast.makeText(context, "FINISH", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return builder.show();
    }

    public static String convertThrowable (Throwable ex) {
        String s = "";
        s += getThrowableStackTrack(ex);
        return s;
    }

    private static String getThrowableStackTrack (Throwable ex) {
        if (ex == null) return null;
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static String convertThrowableShort (Throwable throwable) {
        if (throwable == null) return "";
        if (checkIsIOException(throwable)) return "Network Error";
        return throwable.getMessage();
    }

    public static Snackbar convertSnackbar (final Snackbar snackbar,
                                            @Nullable final Throwable ex) {
        Log.i(TAG, "convertSnackbar");
        if (ex == null) {
            Log.w(TAG, "convertSnackbar -> Throwable is null");
            return snackbar;
        } else {
            Log.i(TAG, "convertSnackbar -> add action");
            snackbar.setAction("action", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Snackbar on click");
                    showErrorDialog(ex, snackbar.getContext());
                }
            });
        }
        return snackbar;
    }
}
