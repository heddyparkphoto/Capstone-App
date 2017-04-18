package com.is.was.be.wannareddit.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.is.was.be.wannareddit.DetailActivity;
import com.is.was.be.wannareddit.R;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.WidgetColumns;

/**
 * Created by hyeryungpark on 4/10/17.
 */

public class WidgetMainService extends RemoteViewsService {

    private static final String LOG_TAG = WidgetMainService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
       /*
            construct the factory that is "Adapter-like" methods implementation
         */
        RemoteViewsFactory factory = new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(ForRedditProvider.WidgetContract.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data!=null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                int t=data==null?0:data.getCount();
                return data==null?0:data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_one_view);

                String subreddit = data.getString(data.getColumnIndex(WidgetColumns.SUBRED));
                String postId = data.getString(data.getColumnIndex(WidgetColumns.POSTID));
                String postTitle = data.getString(data.getColumnIndex(WidgetColumns.POST));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, postTitle);
                }

                views.setTextViewText(R.id.widget_one_id, postTitle);

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(DetailActivity.POSTID, postId);
                fillInIntent.putExtra(DetailActivity.SUBNAME, subreddit);
                views.setOnClickFillInIntent(R.id.widget_one_id, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {

                return new RemoteViews(getPackageName(), R.layout.widget_one_view);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

        return factory;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_one_id, description);
    }


}
