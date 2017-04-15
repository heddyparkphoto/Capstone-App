package com.is.was.be.wannareddit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.ListColumns;
import com.is.was.be.wannareddit.service.WannaTaskService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LoaderManager.LoaderCallbacks<Cursor>,
        MainPagerFragment.OnPostItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private final static String TAG = MainActivity.class.getSimpleName();

    // Play service variables
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected GoogleApiClient mGoogleApiClient;
    // Declare variables for pending intent and fence receiver.
    private final static String FENCE_RECIEVER_ACTION = "FENCE_RECIEVER_ACTION";
    private PendingIntent myPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    AwarenessFence mTimeFence;
    private long mPrefTimeFenceMinutes;

    @BindView(R2.id.main_content) CoordinatorLayout mCoorLayout;
    @BindView(R2.id.toolbar) Toolbar toolbar;
    @BindView(R2.id.interpolator_spinner) Spinner spinner;
    @BindView(R2.id.container) ViewPager mViewPager;   //The {@link ViewPager} that will host the section contents.

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Subreddit table handlers
    private static final int LOADER_ID = 22;
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    public String mCurrentSubredditChoice;
    private ArrayList<String> mSpinnerList;
    private int mSpinnerIdx;

    // Following codes added during Tablet Lesson
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        /*
            Popping the spinner from cursor - Credit to this site: Thanks to:
            http://codetheory.in/understanding-and-populating-android-spinners/
            (A note about the site though, there is a ziggling AD to the right on this site)
         */
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.spinner_text_control,
                mCursor,
                new String[]{ListColumns.SUBREDDITNAME},
                new int[]{R.id.itemInSpinner},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // initialize to set the mAdapter to our spinner
        loadSpinner();

        placeSubredditCurrent();



        /*
        &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
        Detail fragment
         */

        MainPost post=null;
        if (getIntent()!=null) {

            Intent intent = getIntent();
            if (intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT) != null) {
                Bundle bundle = intent.getBundleExtra(DetailFragment.EXTRA_ON_INTENT);
                if (bundle.getParcelable(DetailFragment.PARCEL_ON_ARG) != null) {
                    post = bundle.getParcelable(DetailFragment.PARCEL_ON_ARG);
                }
            }
        }

        if (findViewById(R.id.detailcontainer_fragment) != null){
            mTwoPane = true;

            if (savedInstanceState == null) {
                DetailFragment df = new DetailFragment();
                if (post != null){
                    Bundle args = new Bundle();
                    args.putParcelable((DetailFragment.PARCEL_ON_ARG), post);
                    df.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailcontainer_fragment, df, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        buildMyAwarenessGclient();

        // Schedule Periodic Task for Widgets (requires user settings of subreddit and
        // category from the SharedPreferences)
        // for now we'll hard-code test values
        Bundle bundle = new Bundle();
        bundle.putString(DataUtility.SRDD_PARAM, "explainlikeimfive");
        bundle.putString(DataUtility.CATEG_PARAM, "hot");

        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(WannaTaskService.class)
                .setExtras(bundle)
                .setUpdateCurrent(true)     // Read that this true ensures task is built if there's none
                .setFlex(10L)
                .setPeriod(3600L)           // once per hour 60min*60sec = 3600 long type - test with 30L
//                .setPeriod(60L)           // TEST - one minute!!!
                .setTag(DataUtility.PERIODIC_TAG)
                .build();
        if (checkPlayServices()) {
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
            // and run once right away
        }

        // instantiate variables for AwarenessFence api
        Intent intent = new Intent(FENCE_RECIEVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECIEVER_ACTION));

        // Googleclient builder build - must connect in onStart()
        buildMyAwarenessGclient();
        // Instantiate AwarenssFences
        buildMyAwarenessFence();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);

            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);

            startActivity(intent);

            return true;
        }

        if (id == R.id.refresh) {
//            Intent intent = new Intent(this, TabsActivity.class);
//            startActivity(intent);
            return true;
        }

        if (id == R.id.add) {
            // Subreddit managing activity - Use Dialog
            Intent intent = new Intent(this, SubredditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        SharedPreferences shared = getDefaultSharedPreferences(this);
        shared.registerOnSharedPreferenceChangeListener(this);

        // STILL PROBLEM - WE DON'T WANT TO CHANGE if user is reading another subreddit when the device rotated
        // FIX LATER!!!
        placeSubredditCurrent();

        if (mPrefTimeFenceMinutes < 999L) {
            registerFence("timeFenceKey");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        unregisterFence("timeFenceKey");
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mCurrentSubredditChoice = sharedPreferences.getString(getString(R.string.pref_subrdd_key), "DEFAULT");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            unregisterReceiver(myFenceReceiver);      // Error "not registered"
        } catch (Exception allEx){
            Log.e(TAG, ""+ allEx);  // Catch the exception until I find a way to peek 'registered' state
        }

        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildMyAwarenessGclient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApi connected!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()){
            Log.i(TAG, "Activity Succeeded");
        } else {
            Log.e(TAG, "Activity detection failed: "+status.toString());
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Load data into the Spinner
        return new CursorLoader(this, ForRedditProvider.MainContract.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Load data into the Spinner
        mAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
    private void loadSpinner() {

        // Specify the layout to use when the list of choices appears
        if (mAdapter!=null) {
            mAdapter.setDropDownViewResource(R.layout.spinner_textview);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mCurrentSubredditChoice = ((TextView) view).getText().toString();
//                Toast.makeText(getApplicationContext(), mCurrentSubredditChoice, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinner.setAdapter(mAdapter);

    }

    private void placeSubredditCurrent(){

        SharedPreferences shared = getDefaultSharedPreferences(this);

        if (shared!=null) {
            String combinedPref = shared.getString(getString(R.string.pref_subrdd_key), "DEFAULT");
            int pos = combinedPref.indexOf("|");
            String prefSub = "";
            if (pos > 0) {
                prefSub = combinedPref.substring(0, pos);
                try {
                    mSpinnerIdx = Integer.parseInt(combinedPref.substring(pos + 1));
                } catch (NumberFormatException e) {

                }
            }
            mCurrentSubredditChoice = prefSub;
        }

        if (spinner!=null && mSpinnerIdx > -1){
            spinner.setSelection(mSpinnerIdx);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        // For now, afford 5 categories that are similar to each other and more unique to the 'reddit'
        String[] fiveCategories = {"HOT", "NEW", "RISING", "CONTROVERSIAL", "TOP"};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MainPagerFragment instance for each category - important: category names
            // are part of main recycler view's parameter when invoking reddit public api.
            String inLowerCase = fiveCategories[position].toLowerCase();

            return MainPagerFragment.newInstance(position + 1, inLowerCase, mCurrentSubredditChoice);
        }

        @Override
        public int getCount() {
            // This matches the available tabs
            return fiveCategories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return fiveCategories[0];
                case 1:
                    return fiveCategories[1];
                case 2:
                    return fiveCategories[2];
                case 3:
                    return fiveCategories[3];
                case 4:
                    return fiveCategories[4];

                default:
                    return fiveCategories[0];
            }
        }
    }

    @Override
    public void OnPostItemClick(MainPost post) {

        if (mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, post);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailcontainer_fragment, df, DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.PARCEL_ON_ARG, post);

            intent.putExtra(DetailFragment.EXTRA_ON_INTENT, args);

            startActivity(intent);
        }
    }

    /*
        Google Play Service - If user opts in a time aware reminder
        this receiver does the job
     */
    public class MyFenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), "timeFenceKey")) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        Log.i(TAG, "TimeFence passe " + mPrefTimeFenceMinutes  + " minutes.");
                        Snackbar mSnackbar = Snackbar.make(mCoorLayout,
                                "TimeFence passe " + mPrefTimeFenceMinutes  + " minites.", Snackbar.LENGTH_LONG);
                        mSnackbar.setAction("OK",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        unregisterFence("timeFenceKey");
                                    }
                                });
                        mSnackbar.show();
                        buildMyAwarenessFence();    // repeat
                        break;
                    case FenceState.FALSE:
                        Log.i(TAG, "TimeFence not yet " + mPrefTimeFenceMinutes  + " munite.");
                        break;
                    case FenceState.UNKNOWN:
                        Log.i(TAG, "The TimeFence fence is in an unknown state.");
                        break;
                }
            }

        }
    }

    protected void buildMyAwarenessFence() {
        long nowMillis = System.currentTimeMillis();
        long oneHourMillis = 1L * 60L * 60L * 1000L;
        long oneMinuteMillis = 4L * 60L * 1000L;
        final long multiplyForMillis = 60L * 1000L;

        mPrefTimeFenceMinutes = DataUtility.getTimeFencingTimePreference(this);
        if (mPrefTimeFenceMinutes < 999L) {
            long fenceMillis = mPrefTimeFenceMinutes * multiplyForMillis;
            mTimeFence = TimeFence.inInterval(nowMillis + fenceMillis, Long.MAX_VALUE);
        } else {
            // 999 is a flag that user set to 'Never' to remind time lapsed
            Log.i(TAG, "Fence NEVER was preferred. Try unregistering both receiver and the Fence");
            try {
                unregisterReceiver(myFenceReceiver);      // Error "not registered"
            } catch (Exception allEx){
                Log.e(TAG, ""+ allEx);  // Catch the exception until I find a way to peek 'registered' state
            }
            unregisterFence("timeFenceKey");
        }
    }


    private void registerFence(final String keyname) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(keyname, mTimeFence, myPendingIntent)
                        .build()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()){
                    Log.i(TAG, "Fence successfully registered.");
                } else {
                    Log.i(TAG, "Fence could not be registered. " + status);
                }
            }
        });
    }

    protected void unregisterFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }



}
