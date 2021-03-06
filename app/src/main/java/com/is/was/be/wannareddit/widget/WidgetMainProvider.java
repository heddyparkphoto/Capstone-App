package com.is.was.be.wannareddit.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.is.was.be.wannareddit.DetailActivity;
import com.is.was.be.wannareddit.MainActivity;
import com.is.was.be.wannareddit.R;
import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.service.WannaIntentService;
import com.is.was.be.wannareddit.service.WannaTaskService;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class WidgetMainProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(WannaTaskService.POST_WIDGET_DATA_UPDATED)) {
            Log.d("WidgetMainProvider", "Widget got the update notify");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_id_list);
        } else {
            Log.d("WidgetMainProvider", "NOT THE ACTION...");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetOne: appWidgetIds){

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_main_layout);

            // Click enables widget title bar to load the MainActivity UI
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            rv.setOnClickPendingIntent(R.id.widget_title_txt, pendingIntent);

            // Click enables widget refresh icon to kickoff an immediate update using explicit Service Intent
            Intent refreshIntent = new Intent(context, WannaIntentService.class);
            refreshIntent.putExtra("tag", DataUtility.RUNNOW_TAG);
            refreshIntent.putExtra(DataUtility.CATEG_PARAM, "hot");
            PendingIntent refreshPendingIntent = PendingIntent.getService(
                    context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent);

            // setRemoteAdapter() method obtains the RemoteViewsFactory declared in the WidgetMainService.java
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                setRemoteAdapter(context, rv);
            } else {
                setRemoteAdapterV11(context, rv);
            }

            // Use boolean resource in values-sw600dp to switch which Activity to load in the Template,
            // and if master/detail UI, pass in argument for both panels have data
            boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);

            // Create an intent that launches each graph in DetailFragment
            Intent clickIntent = useDetailActivity?
                    new Intent(context, DetailActivity.class):
                    new Intent(context, MainActivity.class);

            PendingIntent pendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            rv.setPendingIntentTemplate(R.id.widget_id_list, pendingIntentTemplate);
            appWidgetManager.updateAppWidget(appWidgetOne, rv);
        }
    }

    /*
        Sets the remote adapter used to fill in the one item

        @param views RemoteViews to set the RemoteAdapter
*/
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter( R.id.widget_id_list,
                new Intent(context, WidgetMainService.class));
    }

    /*
        Sets the remote adapter used to fill in the one item

        @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_id_list,
                new Intent(context, WidgetMainService.class));
    }
}
