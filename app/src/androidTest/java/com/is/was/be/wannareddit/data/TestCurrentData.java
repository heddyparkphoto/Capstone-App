package com.is.was.be.wannareddit.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.is.was.be.wannareddit.data.ListColumns.SUBREDDITNAME;
import static com.is.was.be.wannareddit.data.WidgetColumns.POST;
import static com.is.was.be.wannareddit.data.WidgetColumns.POSTID;
import static com.is.was.be.wannareddit.data.WidgetColumns.SUBRED;

/**
 * Created by hyeryungpark on 4/5/17.
 */

@RunWith(AndroidJUnit4.class)
public class TestCurrentData {

    static final Context mContext;
    static final private String TAG = "TestCurrentData";

    static {
        mContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void testMainTableData(){

        Uri uri = ForRedditProvider.MainContract.CONTENT_URI;
        Cursor selectAllC = mContext.getContentResolver().query(uri,
                null, null, null, null);

//        assertNull("okay nothing yet :) ", selectAllC);

        if (selectAllC!=null) {
            Log.v(TAG, "" + selectAllC.getCount());
            int ix = selectAllC.getColumnIndex(SUBREDDITNAME);

            while (selectAllC.moveToNext()) {
                Log.v(TAG, "" + ix + " : " + selectAllC.getString(ix));
            }
//        DatabaseUtils.dumpCursor(selectAllC);
//        assertEquals(0, selectAllC.getCount());
            Assert.assertTrue(selectAllC.getCount() > 0);
        }
    }



    @Test
    public void testWidgetTableData(){

        Uri uri = ForRedditProvider.WidgetContract.CONTENT_URI;
        Cursor c = mContext.getContentResolver().query(uri,
                new String[]{SUBRED, POSTID, POST}, null, null, null);

//        assertNull("okay nothing in Widget table yet :) ", c);

        if (c!=null){
        Log.v(TAG, ""+ c.getCount());
        int ix = c.getColumnIndex(SUBRED);
        int ix1 = c.getColumnIndex(POSTID);
        int ix2 = c.getColumnIndex(POST);

        while (c.moveToNext()){
            Log.v(TAG, "" + ix + ": " + c.getString(ix) + " " + ix1 + ": "+ c.getString(ix1) + " " + ix2 + ": " + c.getString(ix2));
        }
//        DatabaseUtils.dumpCursor(selectAllC);
//        assertEquals(0, selectAllC.getCount());
        Assert.assertTrue(c.getCount()== 0);
//        assertNull("okay nothing yet :) ", selectAllC);
    }
    }

}
