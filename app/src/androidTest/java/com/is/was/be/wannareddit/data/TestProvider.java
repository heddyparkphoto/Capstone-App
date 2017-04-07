package com.is.was.be.wannareddit.data;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by hyeryungpark on 4/5/17.
 */

@RunWith(AndroidJUnit4.class)
public class TestProvider {
    static final Context mContext;
    static final private String TAG = "TestProvider";

    static {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testBuildUriBase() {
        Uri uri = ForRedditProvider.MainContract.CONTENT_URI;

        Uri uriwi = ForRedditProvider.WidgetContract.CONTENT_URI;

        // The value similar to: content://com.learn.heddy.myrecognizedsample.data.ForRedditProvider/sub_with_id/16
        assertTrue(uri.toString().equals("content://com.is.was.be.wannareddit.data.ForRedditProvider/subredditnames"));
//        assertTrue(uri.toString().equals("content://com.is.was.be.wannareddit.data.ForRedditProvider/subreddits"));
        assertTrue(uriwi.toString().equals("content://com.is.was.be.wannareddit.data.ForRedditProvider/widgetposts"));
    }

    @Test
    public void testInsertIntoSub() {
        /* Since we end up inserting, first delete all rows in the SUBRED names table */
//        Uri uriClean = ForRedditProvider.MainContract.CONTENT_URI;
//        int num = mContext.getContentResolver().delete(uriClean,
//                null, null);
//        Log.d(TAG, "CheckSum: deleted rows for this testing: "+num);
//
//        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//
//        // Add 5 test rows into TABLE_SUBREDDIT
//        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
//                ForRedditProvider.MainContract.CONTENT_URI);
//        operations.add(builder.withValue(SUBREDDITNAME, "Futurology").build());
//
//        builder = ContentProviderOperation.newInsert(
//                ForRedditProvider.MainContract.CONTENT_URI);
//        operations.add(builder.withValue(SUBREDDITNAME, "shutupandtakemymoney").build());
//
//        builder = ContentProviderOperation.newInsert(
//                ForRedditProvider.MainContract.CONTENT_URI);
//        operations.add(builder.withValue(SUBREDDITNAME, "todayilearneD").build());
//
//        builder = ContentProviderOperation.newInsert(
//                ForRedditProvider.MainContract.CONTENT_URI);
//        operations.add(builder.withValue(SUBREDDITNAME, "Documentaries").build());
//
//        builder = ContentProviderOperation.newInsert(
//                ForRedditProvider.MainContract.CONTENT_URI);
//        operations.add(builder.withValue(SUBREDDITNAME, "art").build());
//
//        try {
//            mContext.getContentResolver().applyBatch(ForRedditProvider.AUTHORITY, operations);
//
//            Uri uri = ForRedditProvider.MainContract.CONTENT_URI;
//            Cursor selectAllC = mContext.getContentResolver().query(uri,
//                    null, null, null, null);
//            assertEquals(5, selectAllC.getCount());
//
//            DatabaseUtils.dumpCursor(selectAllC);
//
//        } catch (RemoteException | OperationApplicationException someexception){
//            Log.e("testInsertIntoSub", ""+ someexception);
//        }
    }



}
