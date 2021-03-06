package com.is.was.be.wannareddit.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.android.gms.gcm.TaskParams;
import com.is.was.be.wannareddit.data.DataUtility;

/**
 * Created by hyeryungpark on 4/16/17.
 */

public class WannaIntentService extends IntentService {

    public WannaIntentService() {
        super(WannaIntentService.class.getName());
    }

    public WannaIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        WannaTaskService taskService = new WannaTaskService(this);
        String tagToPassIn = intent.getStringExtra("tag");

        Bundle args = new Bundle();

        if (tagToPassIn!=null && tagToPassIn.equals(DataUtility.RUNNOW_TAG)){

            args.putString(DataUtility.CATEG_PARAM, intent.getStringExtra(DataUtility.CATEG_PARAM));
        } else if (intent.getStringExtra("tag").equals(DataUtility.ADD_TAG)) {

            args.putString(DataUtility.SRDD_PARAM, intent.getStringExtra(DataUtility.SRDD_PARAM));
        }

        // If an Activity that would like to receive the result if "add" operation fails sent receiver extra
        final ResultReceiver receiver = intent.getParcelableExtra(DataUtility.RECEIVER);
        Bundle bundle = new Bundle();

        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        int result = taskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

        // Activity would like to handle the result if "add" operation fails
        if (receiver!=null) {
                receiver.send(result, bundle);
        }
    }
 }
