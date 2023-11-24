package com.demo.opencv.infoUtil;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PhotoUtils {
    //from albums
    public String handleImageOnKitKat(Context context, Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        //If it is of type Document, it is handled with document id, if it is of type content uri is handled in the normal way
        if(DocumentsContract.isDocumentUri(context, uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; //Parsing out ids in numeric format
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(context, contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(context, uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return imagePath;
    }
    public String handleImageBeforeKitKat(Context context, Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(context,uri, null);
        return imagePath;
    }
    //Private method
    private String getImagePath(Context context,Uri uri, String selection){
        String path = null;
        //Get the real image path with Uri and selection
        Cursor cursor = context.getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    //Bitmap to byte[]
    public byte[] bitmap2byte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    //byte[] to Bitmap
    public Bitmap byte2bitmap(byte[] bytes){
        if (bytes != null && bytes.length != 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    //Pass in the file name and read the file from assets
    public byte[] file2byte(Context context, String filename){
        try {
            InputStream is = context.getAssets().open(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap2byte(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
