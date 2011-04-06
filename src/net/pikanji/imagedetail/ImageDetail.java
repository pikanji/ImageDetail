
package net.pikanji.imagedetail;

import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDetail extends Activity implements OnClickListener {
    private static final String DEBUG_TAG = "ImageDetail";
    private static final int REQUEST_PICK_IMAGE = 531;
    private final String mNewLine;
    private String mInfo = "";
    private Bitmap mThumbnail;

    // インテントからの情報の格納先を準備する
    private static final String[] projection = {
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DESCRIPTION, MediaStore.Images.ImageColumns.IS_PRIVATE,
            MediaStore.Images.ImageColumns.LATITUDE, MediaStore.Images.ImageColumns.LONGITUDE,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.Images.ImageColumns.PICASA_ID,
            MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns._ID,
    // MediaStore.Images.ImageColumns._COUNT,
    };

    public ImageDetail() {
        mNewLine = System.getProperty("line.separator");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Button button = (Button) findViewById(R.id.button_select);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        TextView textView = (TextView) findViewById(R.id.text_info);
        textView.setText(mInfo);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setImageBitmap(mThumbnail);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_PICK_IMAGE != requestCode) {
            return;
        }

        if (null == data) {
            return;
        }

        Uri uri = data.getData();
        Cursor cursor = this.managedQuery(uri, projection, null, null, null);
        mInfo = getInfoFromDatabase(cursor);

        String filepath = cursor.getString(10);
        mInfo += getInfoFromExif(filepath);

        TextView textView = (TextView) findViewById(R.id.text_info);
        textView.setText(mInfo);

        long imageId = cursor.getLong(17);
        ContentResolver cr = getContentResolver();
        mThumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, imageId, Thumbnails.MINI_KIND,
                null);
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setImageBitmap(mThumbnail);
    }

    private String getInfoFromDatabase(Cursor cursor) {
        String info = "";
        if ((null == cursor) || !cursor.moveToFirst()) {
            return info;
        }
        info += "-- DB ----" + mNewLine;
        info += "bucket display name: " + cursor.getString(0) + mNewLine;
        info += "bucket ID: " + cursor.getString(1) + mNewLine;
        info += "date taken: " + formatDate(cursor.getLong(2)) + mNewLine;
        info += "description: " + cursor.getString(3) + mNewLine;
        info += "is private: " + cursor.getInt(4) + mNewLine;
        info += "latitude: " + cursor.getDouble(5) + mNewLine;
        info += "longitude: " + cursor.getDouble(6) + mNewLine;
        info += "mini thumb magic: " + cursor.getInt(7) + mNewLine;
        info += "orientation: " + cursor.getInt(8) + mNewLine;
        info += "picasa ID: " + cursor.getString(9) + mNewLine;

        info += "file path: " + cursor.getString(10) + mNewLine;
        info += "data added: " + formatDate(cursor.getLong(11)) + mNewLine;
        info += "data modified: " + formatDate(cursor.getLong(12)) + mNewLine;
        info += "display name: " + cursor.getString(13) + mNewLine;
        info += "mime_type: " + cursor.getString(14) + mNewLine;
        info += "size: " + cursor.getInt(15) / 1024 + "KB" + mNewLine;
        info += "title: " + cursor.getString(16) + mNewLine;

        info += "id: " + cursor.getLong(17) + mNewLine;
        // info += "row count: " + cursor.getString(18) + mNewLine;
        info += mNewLine;
        return info;
    }

    private String getInfoFromExif(String filepath) {
        String info = "";
        ExifInterface exif;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "ExifInterface caused exeption.");
            return info;
        }

        info += "-- exif ----" + mNewLine;
        // 2.1
        info += "date time: " + exif.getAttribute(ExifInterface.TAG_DATETIME) + mNewLine;
        info += "flash: " + exif.getAttribute(ExifInterface.TAG_FLASH) + mNewLine;
        info += "GPS latitude: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + mNewLine;
        info += "GPS latitude ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                + mNewLine;
        info += "GPS longitude: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + mNewLine;
        info += "GPS longitude ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                + mNewLine;
        info += "image length: " + exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) + mNewLine;
        info += "image width: " + exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) + mNewLine;
        info += "make: " + exif.getAttribute(ExifInterface.TAG_MAKE) + mNewLine;
        info += "model: " + exif.getAttribute(ExifInterface.TAG_MODEL) + mNewLine;
        info += "orientation: " + exif.getAttribute(ExifInterface.TAG_ORIENTATION) + mNewLine;
        info += "white balance: " + exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE) + mNewLine;
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.HONEYCOMB: // 3.0
                info += "aperture: " + exif.getAttribute(ExifInterface.TAG_APERTURE) + mNewLine;
                info += "exposure time: " + exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                        + mNewLine;
                info += "ISO: " + exif.getAttribute(ExifInterface.TAG_ISO) + mNewLine;
            case Build.VERSION_CODES.GINGERBREAD: // 2.3.1
                info += "GPS altitude: " + exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)
                        + mNewLine;
                info += "GPS altitude ref: "
                        + exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF) + mNewLine;
            case Build.VERSION_CODES.FROYO: // 2.2
                info += "focal length: " + exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                        + mNewLine;
                info += "GPS date stamp: " + exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)
                        + mNewLine;
                info += "GPS proccessing method: "
                        + exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) + mNewLine;
                info += "GPS time stamp: " + exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP)
                        + mNewLine;
        }
        return info;
    }

    private String formatDate(long dateMillis) {
        int format = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        return DateUtils.formatDateTime(this, dateMillis, format);
    }
}
