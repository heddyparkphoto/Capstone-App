package com.is.was.be.wannareddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.ListColumns;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.is.was.be.wannareddit.data.ListColumns.SUBREDDITNAME;

public class SubredditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{

    private SubredditAdapter mSubAdapter;
    private static final int LOADER_ID = 30;

    protected Context mContext;

    @BindView(R2.id.fab) FloatingActionButton mFab;
    @BindView(R2.id.recyclerview_subreddit_empty) TextView emptyTxtView;
    @BindView(R2.id.recyclerview_subreddit) RecyclerView mRecylerView;

    @BindString(R2.string.a11y_add_success) String addSuccessMsgIncomplete;
    @BindString(R2.string.a11y_dupe_noadd) String dupeMsgIncomplete;
    @BindString(R2.string.title_add_srddt) String strRscTitle;
    @BindString(R2.string.content_add_srddt) String strRscContent;
    @BindString(R2.string.hint_add_srddt) String strRscHint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        mContext = this;

        ButterKnife.bind(this);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        mSubAdapter = new SubredditAdapter(this, emptyTxtView);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this));
        mRecylerView.setAdapter(mSubAdapter);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mContext).title(strRscTitle)
                        .content(strRscContent)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(strRscHint, "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String confirmStr = String.format(addSuccessMsgIncomplete, input.toString());
                                Toast.makeText(mContext, confirmStr, Toast.LENGTH_SHORT).show();
//                                // Receive user input. Make sure the subreddit doesn't already exist
//                                // in the DB and proceed accordingly - our DB is case-sensitive we'll keep as user types in
//                                // keep duplicates as well - it won't be a huge collection anyhow - they're not case-sensitive on reddit
//
                                Cursor c = mContext.getContentResolver().query(ForRedditProvider.MainContract.CONTENT_URI,
                                        new String[]{ListColumns.SUBREDDITNAME}, ListColumns.SUBREDDITNAME + "= ?",
                                        new String[]{input.toString()}, null);
                                if (c.getCount() != 0) {
                                    String dupeStr = String.format(dupeMsgIncomplete, input.toString());
                                    Toast toast =
                                            Toast.makeText(mContext, dupeStr, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                    toast.show();

                                    c.close();
                                    return;
                                } else {
                                    if (c!=null){
                                        c.close();
                                    }

                                    // Add the subreddit to DB
                                    // For now directly?? is this dangerous?
                                    ContentValues cv = new ContentValues(1);
                                    cv.put(SUBREDDITNAME, input.toString());
                                    Uri uri = mContext.getContentResolver().insert(
                                            ForRedditProvider.MainContract.CONTENT_URI, cv
                                            );

//                                    mServiceIntent.putExtra("tag", TaskTagKind.ADD);
//                                    mServiceIntent.putExtra("symbol", input.toString());
//
//                                    mResultReceiverHelper = new TaskHelper(new Handler());
//                                    mResultReceiverHelper.setReceiver(
//                                            new TaskHelper.Receiver() {
//                                                @Override
//                                                public void onReceiveResult(int resultCode, Bundle resultData) {
//                                                    String msg = resultData.getString(Intent.EXTRA_TEXT);
//                                                    if (resultCode==StockTaskService.INVALID_NAME){
//                                                        msg = String.format(Locale.US, getString(R.string.subreddit_not_found), input);
//                                                    }
//                                                    Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
//                                                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
//                                                    toast.show();
//                                                }
//                                            });
//
//                                    mServiceIntent.putExtra(RECEIVER, mResultReceiverHelper);
//                                    mContext.startService(mServiceIntent);
                                }
                            }
                        }).show();
            }
        });

     }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this, ForRedditProvider.MainContract.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSubAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class SubredditAdapter extends RecyclerView.Adapter<SubredditAdapter.SubViewHolder> {
        final private static String TAG = "SubAdapter";
        final private Context mContext;
        TextView mEmptyTextView;
        private Cursor mCursor;

        public SubredditAdapter(Context context, TextView emptyView) {
            mContext = context;
            mEmptyTextView = emptyView;
        }

        public class SubViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            Button rmButton;

            public SubViewHolder(View view) {
                super(view);
                nameView = (TextView) view.findViewById(R.id.sub_name);
                rmButton = (Button) view.findViewById(R.id.rm_button);
                rmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String rmConfStr = String.format(getResources().getString(R.string.a11y_remove_confirm), nameView.getText().toString());
                        // get the subreddit name from this passedin button's accompanying nameView.getText()
                        int checksum = mContext.getContentResolver().delete(
                                ForRedditProvider.MainContract.CONTENT_URI,
                                " " + SUBREDDITNAME + "=?", new String[]{nameView.getText().toString()}
                                );
                        if (checksum==1) {
                            Toast.makeText(mContext, rmConfStr, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Remove Db error-Deleted row num returned: "+checksum);
                        }
                    }
                });
            }
        }

        @Override
        public SubredditAdapter.SubViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (parent instanceof RecyclerView) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_subreddit, parent, false);
                view.setFocusable(true);

                SubredditAdapter.SubViewHolder holder = new SubredditAdapter.SubViewHolder(view);
                return holder;
            } else {
                throw new RuntimeException("Error ** Not bound to RecyclerView Selection!!");
            }
        }

        @Override
        public void onBindViewHolder(SubredditAdapter.SubViewHolder holder, int position) {

            mCursor.moveToPosition(position);
            int colIx = mCursor.getColumnIndex(SUBREDDITNAME);
            holder.nameView.setText(mCursor.getString(colIx));

            // what todo with the button??
        }

        @Override
        public int getItemCount() {
            if (mCursor!=null){
                return mCursor.getCount();
            }
            return 0;
        }

        public void swapCursor(Cursor data){
            /*
                swapCursor builds the mCursor!!!
            */
                mCursor = data;

                if (getItemCount()==0){
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                }
                notifyDataSetChanged();
            }

        }
    }

