package com.zmxv.RNSound;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

interface OnRNDownloadTaskListener {
    void onDownloadFinishedEvent(String outputFile, Double key, String error);
}


public class RNDownloadTask {

    private static final String TAG = "Download Task";
    private Context _context;
    private String _downloadUrl = "", _downloadFileName = "";
    private OnRNDownloadTaskListener _listener;
    private Double _key;

    public RNDownloadTask(Context context, String downloadUrl, Double key, OnRNDownloadTaskListener listener) {
        _context = context;
        _downloadUrl = downloadUrl;
        _key = key;
        _downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/')+1);//Create file name by picking download file name from URL
        Log.e(TAG, _downloadFileName);
        _listener = listener;
        //Start Downloading Task
        new DownloadingTask().execute();
    }


    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        File _fileStorage = null;
        File _outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (_outputFile != null) {
                   //Download completed
                    _listener.onDownloadFinishedEvent(_outputFile.getPath(), _key,null);
                } else {
                    //download failed change button text
                    _listener.onDownloadFinishedEvent(null, _key,"Download Failed");
                    Log.e(TAG, "Download Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();

                //exception occurs
                _listener.onDownloadFinishedEvent(null, _key,"Download Failed");
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                URL url = new URL(_downloadUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());

                }


                _fileStorage = new File(
                        _context.getFilesDir() + Constants.downloadFolder);

                //If File is not present create directory
                if (!_fileStorage.exists()) {
                    _fileStorage.mkdir();
                    Log.e(TAG, "Directory Created.");
                }

                _outputFile = new File(_fileStorage, _downloadFileName);//Create Output file in Main File

                //Create New File if not present
                if (!_outputFile.exists()) {
                    _outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }

                FileOutputStream fos = new FileOutputStream(_outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                fos.close();
                is.close();

            } catch (Exception e) {

                //Read exception if something went wrong
                e.printStackTrace();
                _outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }
}