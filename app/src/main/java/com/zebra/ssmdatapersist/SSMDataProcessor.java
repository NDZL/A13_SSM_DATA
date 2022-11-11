package com.zebra.ssmdatapersist;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.UUID;

//the  benchmark branch aims at heavily testing the SSM performance

public class SSMDataProcessor extends AppCompatActivity {

    Uri cpUri;
    ContentProviderClient cpClient;
    private final String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";
    static final String TAG = "SSMDataProcessor";
    int howManyRecords=10;
    long seed=3003;

    private final String COLUMN_ORIG_APP_PACKAGE = "orig_app_package";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_INPUT_FORM = "data_input_form";
    private final String COLUMN_DATA_OUTPUT_FORM = "data_output_form";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_MULTI_INSTANCE_REQUIRED = "multi_instance_required";

    LocalContentObserver myContentObserver;
    LocalDataSetObserver myDataSetObserver;
    Spinner persisFlagSpinner;
    Spinner howManyRecsSpinner;
    Spinner seedSpinner;
    Context mContext = null;

    // NDZL DEBUG SIGNATURE ON AS-DELL
    private final String signature = "MIIC5DCCAcwCAQEwDQYJKoZIhvcNAQEFBQAwNzEWMBQGA1UEAwwNQW5kcm9pZCBEZWJ1ZzEQMA4GA1UECgwHQW5kcm9pZDELMAkGA1UEBhMCVVMwIBcNMjIxMDEyMDk1NjM5WhgPMjA1MjEwMDQwOTU2MzlaMDcxFjAUBgNVBAMMDUFuZHJvaWQgRGVidWcxEDAOBgNVBAoMB0FuZHJvaWQxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAha0WrWTMN8Yh40qZ9dX9f2FvyWwpdX8T0L8khTCs6bydqbKVSQOoe28g2y9uA5lbhor4nRstJwVY39TKC6v3ESQHIw2ESxoxU6oMVyqxKlOw48mW3fBGVH8A220Zm0Yo4ibmH4E61ahg5sbdxq2cfoizxCfDuRE7+78/kn/na6CdTH9vBlJ8MJpv587jsV78OI7+vT7bnd7PRmx8D3vxsEfdw0BzPA/C+hovy3y2jMFUe36wXZEn4hdIxqAIngeemFabEyAj5ViSvX6LcPdgUmlcrTyapz0QkjpJHrvOkBXwtwCntAESvVIJHkYnZHgMLSXml6MLlklybQGzGOrPRQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQAE80M6+8TrJW/74A1DFkdE21ZetggUc47WG1U5R5HBw+6BLdHy/MZtyN1H9eL3TIICPuL4QXR6BCEp/iWzRwjukopwmwFhzCo2IgKmQpidkaFSdLutETwtp04L3PaXjbVxeGkhMVkYDjtbB6xbZx/ioShQ+bKvbmNOQxNdktyCvcx7s8BhzWtcPPmzYSFt0DEk2n4br2yWf9VUQBKgbjJpo/yoKWrCbb4Wu/WtHGOXGNy2r0FLkiocWHL7liGtAN+rpo0wRZtPoPYxxikqUY+ZOu4rXDu1WeLgbrpJjT84PKO/BJ8zfTD0F2nGZZaz3HjBikEjXxsoziZ/axBdfhmJ";


    static double totalTimesec=0;
    TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         mContext = this.getApplicationContext();
         resultView = findViewById(R.id.result);

        initializeHowManyRecsSpinner();
        initializePersistFlagSpinner();
        initializeSeedSpinner();

        cpUri = Uri.parse(AUTHORITY);
        cpClient = getContentResolver().acquireContentProviderClient(cpUri);

        myContentObserver = new LocalContentObserver(null);
        myDataSetObserver = new LocalDataSetObserver();

        resultView.setText("SSM contains "+ ssm_notpersisted_countRecords()+ " not persisted records\nand "+ssm_ispersisted_countRecords()+" persisted records from this app");
    }

    private void initializePersistFlagSpinner() {
        persisFlagSpinner = (Spinner)findViewById(R.id.persistFlagSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.persist_flag, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        persisFlagSpinner.setAdapter(adapter);
    }


    private void initializeHowManyRecsSpinner() {
        howManyRecsSpinner = (Spinner)findViewById(R.id.howmanyrecsSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.howmanyrec_flag, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        howManyRecsSpinner.setAdapter(adapter);

        howManyRecsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                howManyRecords = Integer.parseInt(howManyRecsSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }


    private void initializeSeedSpinner() {
        seedSpinner = (Spinner)findViewById(R.id.seedSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.seeds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seedSpinner.setAdapter(adapter);

        seedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(seedSpinner.getSelectedItem().toString().equals("Random")){
                    seed = System.currentTimeMillis();
                }
                else
                    seed = Integer.parseInt(seedSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public void onClickInsertData(View view) {
        insertData();
    }

    public void insertData() {
        try {
            ssm_insertMassiveData();
        } catch (Exception e) {
            Log.d(TAG, "Insert - error: " + e.getMessage());
        }
    }


    long timeBegin=0;
    private void ssm_insertMassiveData() {
        String key = "";
        String val = "";
        ContentValues values = new ContentValues();

        //KEYs are generated in a random and repeatable sequence
        if(seedSpinner.getSelectedItem().toString().equals("Random")){
            seed = System.currentTimeMillis();
        }
        Random rand = new Random( seed );

        int insert_success_count=0;
        int insert_error_count=0;
        timeBegin= System.currentTimeMillis();
        for(int idx = 0; idx<howManyRecords; idx++){
            key = ""+rand.nextLong();
            val = UUID.randomUUID().toString();
            String mustPersist=persisFlagSpinner.getSelectedItem().toString();

            values.put(COLUMN_TARGET_APP_PACKAGE, "{\"pkgs_sigs\":[{\"pkg\":\""+ mContext.getPackageName() +"\",\"sig\":\"" + signature + "\"}]}");
            values.put(COLUMN_DATA_NAME, key);
            values.put(COLUMN_DATA_VALUE, val);
            values.put(COLUMN_DATA_TYPE,"1");
            values.put(COLUMN_DATA_INPUT_FORM, "1");
            values.put(COLUMN_DATA_OUTPUT_FORM, "1");
            values.put(COLUMN_DATA_PERSIST_REQUIRED, mustPersist );
            values.put(COLUMN_MULTI_INSTANCE_REQUIRED, "false");


            Uri createdRow = null;
            try {
                createdRow = getContentResolver().insert(cpUri, values);
                insert_success_count++;
                Log.d(TAG, "Row Created : " + createdRow.toString());
            } catch (Exception e) {
                insert_error_count++;
                Log.d(TAG, "Row Create Excp : " + e.getMessage());
            }

        }
        long timeEnd = System.currentTimeMillis();
        totalTimesec = (1. * timeEnd - 1. * timeBegin) / 1000.0;

        //Toast.makeText(mContext, "New Record Inserted", Toast.LENGTH_SHORT).show();
        resultView.setText("Inserted:"+insert_success_count+" records. Failed to insert:"+ insert_error_count+" records in "+totalTimesec+"s\n"+  "SSM status: "+ ssm_notpersisted_countRecords() +" non-persisted records\n "+ssm_ispersisted_countRecords()+" persisted records");
    }


    int ssm_notpersisted_countRecords() {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'false'" + " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        int _count=0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
            _count = cursor.getCount();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return _count;
    }

    int ssm_ispersisted_countRecords() {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'true'" + " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        int _count=0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
            _count = cursor.getCount();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return _count;
    }

    @SuppressLint("Range")
    public void onClickQueryAllDataAtOnce(View view) {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'" +
                " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder strBuild = new StringBuilder();
                while (!cursor.isAfterLast()) {
                    strBuild.append("\n" + "" +
                           // "COLUMN_ORIG_APP_PACKAGE :" + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_ORIG_APP_PACKAGE)) + " \n " +
                            //"COLUMN_TARGET_APP_PACKAGE :" + "\n"  + cursor.getString(cursor.getColumnIndex(COLUMN_TARGET_APP_PACKAGE)) + " \n " +
                            "" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_NAME)) + " : " +
                            "" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_VALUE)) + " \n "+
                            //"COLUMN_DATA_TYPE: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE)) + " \n " +
                            //"COLUMN_DATA_INPUT_FORM: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_INPUT_FORM)) + " \n " +
                            //"COLUMN_DATA_OUTPUT_FORM: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_OUTPUT_FORM)) + " \n " +
                            //"COLUMN_DATA_PERSIST_REQUIRED: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_DATA_PERSIST_REQUIRED)) + " \n " +
                            //"COLUMN_MULTI_INSTANCE_REQUIRED: " + "\n" + cursor.getString(cursor.getColumnIndex(COLUMN_MULTI_INSTANCE_REQUIRED)) +
                            ""
                            );

                    String targetData = cursor.getString(cursor.getColumnIndex(COLUMN_TARGET_APP_PACKAGE));
                    //Log.d(TAG, "Target Data Received: " + targetData);
                    //strBuild.append("\n ----------------------").append("\n");
                    cursor.moveToNext();
                }
                Log.d(TAG, "Query data: " + strBuild);
                resultView.setText(strBuild);
            } else {
                resultView.setText("No Records Found");
            }
        } catch (Exception e) {
            Log.d(TAG, "Query data error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }




    public void onClickDeleteAllData(View view) {
        try {
            Uri cpUriDelete = Uri.parse(AUTHORITY + "/[" + mContext.getPackageName() + "]");

            String whereClauseNotPersisted = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'false'";
            String whereClauseIsPersisted = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'false'";
            timeBegin= System.currentTimeMillis();
            int rc_np = getContentResolver().delete(cpUriDelete, whereClauseNotPersisted, null);
            int rc_ip = getContentResolver().delete(cpUriDelete, whereClauseIsPersisted, null);
            long timeEnd = System.currentTimeMillis();
            totalTimesec = (1. * timeEnd - 1. * timeBegin) / 1000.0;
            resultView.setText("Deleted: "+rc_np+" non-persisted records\nDeleted: "+rc_ip+" persisted records");
        } catch (Exception e) {
            Log.d(TAG, "Delete - error: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(cpUri, true, myContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(myContentObserver);
    }
}


class LocalContentObserver extends ContentObserver {
    public LocalContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
        Log.d(SSMDataProcessor.TAG, "### received self change notification from uri: ");
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(SSMDataProcessor.TAG, "### received notification from uri: " + uri.toString());
    }
}

class LocalDataSetObserver extends DataSetObserver {
    public LocalDataSetObserver() {

    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        Log.d(SSMDataProcessor.TAG, "onInvalidate");
    }

    @Override
    public void onChanged() {
        super.onChanged();
        Log.d(SSMDataProcessor.TAG, "onChanged");
    }
}
