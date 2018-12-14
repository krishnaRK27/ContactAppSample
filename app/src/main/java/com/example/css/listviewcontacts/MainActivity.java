package com.example.css.listviewcontacts;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    SimpleCursorAdapter mAdapter;
    MatrixCursor mMatrixCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMatrixCursor = new MatrixCursor(new String[] { "_id","name","photo","details"} );

        mAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.lv_layout,
                null,
                new String[] { "name","photo","details"},
                new int[] { R.id.tv_name,R.id.iv_photo,R.id.tv_details}, 0);

        ListView lstContacts = (ListView) findViewById(R.id.lst_contacts);
        lstContacts.setAdapter(mAdapter);
        ListViewContactsLoader listViewContactsLoader = new ListViewContactsLoader();
        listViewContactsLoader.execute();
    }


    private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
            if(contactsCursor.moveToFirst()){
                do{
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));
                    Uri dataUri = ContactsContract.Data.CONTENT_URI;
                    Cursor dataCursor = getContentResolver().query(dataUri, null,
                            ContactsContract.Data.CONTACT_ID + "=" + contactId,
                            null, null);

                            String displayName="";
                            String nickName="";
                            String homePhone="";
                            String mobilePhone="";
                            String workPhone="";
                            String photoPath="" + R.mipmap.ic_launcher;
                            byte[] photoByte=null;
                            String homeEmail="";
                            String workEmail="";
                            String companyName="";
                            String title="";

                    if(dataCursor.moveToFirst()){
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME ));
                        do{
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
                                nickName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME :
                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE :
                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK :
                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ) ) {
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME :
                                        homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK :
                                        workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)){
                                companyName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                title = dataCursor.getString(dataCursor.getColumnIndex("data4"));
                            }
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)){
                                photoByte = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));

                                if(photoByte != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoByte, 0, photoByte.length);


                                    File cacheDirectory = getBaseContext().getCacheDir();


                                    File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+contactId+".png");


                                    try {
                                        FileOutputStream fOutStream = new FileOutputStream(tmpFile);


                                        bitmap.compress(Bitmap.CompressFormat.PNG,100, fOutStream);


                                        fOutStream.flush();


                                        fOutStream.close();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    photoPath = tmpFile.getPath();
                                }
                            }
                        }while(dataCursor.moveToNext());
                        String details = "";
                        if(homePhone != null && !homePhone.equals("") )
                            details = "HomePhone : " + homePhone + "\n";
                        if(mobilePhone != null && !mobilePhone.equals("") )
                            details += "MobilePhone : " + mobilePhone + "\n";
                        if(workPhone != null && !workPhone.equals("") )
                            details += "WorkPhone : " + workPhone + "\n";
                        if(nickName != null && !nickName.equals("") )
                            details += "NickName : " + nickName + "\n";
                        if(homeEmail != null && !homeEmail.equals("") )
                            details += "HomeEmail : " + homeEmail + "\n";
                        if(workEmail != null && !workEmail.equals("") )
                            details += "WorkEmail : " + workEmail + "\n";
                        if(companyName != null && !companyName.equals("") )
                            details += "CompanyName : " + companyName + "\n";
                        if(title != null && !title.equals("") )
                            details += "Title : " + title + "\n";



                        mMatrixCursor.addRow(new Object[]{ Long.toString(contactId),displayName,photoPath,details});
                    }
                }while(contactsCursor.moveToNext());
            }
            return mMatrixCursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            mAdapter.swapCursor(result);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}