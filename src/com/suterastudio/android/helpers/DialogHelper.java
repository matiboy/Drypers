/* DialogHelper.java

   Copyright (c) 2010 Ethan Chen

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.suterastudio.android.helpers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DialogHelper {

    public static void showWarning(Context context, String title, String warning) {
        Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setMessage(warning).setTitle(
                title).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        warningBuilder.create().show();
    }

    public static void showConfirmation(Context context, String title, String message,
            int positiveId, int negativeId, OnClickListener clickListener) {
        Builder confirmDialogBuilder = new Builder(context);
        confirmDialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("", clickListener)
                .setNegativeButton("", clickListener);
        confirmDialogBuilder.create().show();
    }
}
