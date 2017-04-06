package com.is.was.be.wannareddit.data;

import android.content.ContentUris;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by hyeryungpark on 4/5/17.
 */

@ContentProvider(authority =  ForRedditProvider.AUTHORITY, database = ForRedditDb.class)
public class ForRedditProvider {

    public static final String AUTHORITY = "com.is.was.be.wannareddit.data.ForRedditProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String PATH_SUBRED = "subreddits";
        String PATH_WIDGET = "widgetposts";
    }

    private static Uri buildUri(String... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path:paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table =  ForRedditDb.TABLE_SUBREDDIT)
    public static class MainContract {

        /* ContentUri is for select everything */
        @ContentUri(
                path = Path.PATH_SUBRED,
                type = "vnd.android.cursor.dir/subreddits"
        )

        /* Invoke this way: MainContract.CONTENT_URI  */
        public static final Uri CONTENT_URI = buildUri(Path.PATH_SUBRED);

        /*
            InexactContentUri selects one subreddit by its _id when one of the favorites row needs
            the name of the subreddit as a request parameter to the reddits.com API when
            fetching detail-content that we don't save in the favorites table but are needed in the UI.
         */
        @InexactContentUri(
                name = Path.PATH_SUBRED,
                path = Path.PATH_SUBRED + "/#",
                type = "vnd.android.cursor.item/subreddits",
                whereColumn = {ListColumns._ID},
                pathSegment = {1}
        )

        // ??? duh ... !!! it's tough for now.
        public static Uri itemOfSubreddit(long id){
            return ContentUris.withAppendedId(buildUri(Path.PATH_SUBRED), id);
        }
    }

    @TableEndpoint(table = ForRedditDb.TABLE_WIDGET)
    public static class WidgetContract {
        @ContentUri(
                path = Path.PATH_WIDGET,
                type = "vnd.android.cursor.dir/widgetposts"
        )

        public static final Uri CONTENT_URI = buildUri(Path.PATH_WIDGET);

        @InexactContentUri(
                name = Path.PATH_WIDGET,
                path = Path.PATH_WIDGET + "/*",
                type = "vnd.android.cursor.item/widgets",
                whereColumn = {WidgetColumns.POSTID},
                pathSegment = {1}
        )

        public static Uri itemOfFavorites(String postId){
            return buildUri(Path.PATH_WIDGET, postId);

        }
    }

}
