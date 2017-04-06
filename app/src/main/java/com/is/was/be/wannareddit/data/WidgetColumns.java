package com.is.was.be.wannareddit.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by hyeryungpark on 4/5/17.
 */

public interface WidgetColumns {
    @DataType(INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";
    @DataType(TEXT) @NotNull String SUBRED = "subname";
    @DataType(TEXT) @NotNull String POSTID = "postid";
    @DataType(TEXT) String POST = "post";
}
