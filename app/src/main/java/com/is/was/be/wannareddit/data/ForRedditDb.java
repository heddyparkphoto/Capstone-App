package com.is.was.be.wannareddit.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by hyeryungpark on 4/5/17.
 */
@Database(version = ForRedditDb.VERSION)
public class ForRedditDb {
    public final static int VERSION = 2;

    @Table(ListColumns.class) public static final String TABLE_SUBREDDIT = "subrddnames";

    @Table(WidgetColumns.class) public static final String TABLE_WIDGET = "widget";
}
