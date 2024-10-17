package omena.guilherme.galeriapublica;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GalleryRepository {
    Context context;

    public GalleryRepository(Context context) {
        this.context = context;
    }

    public List<ImageData> loadImageData(Integer limit, Integer offSet)
            throws FileNotFoundException{
        List<ImageData> imageDataList = new ArrayList<>();
        int w = (int)context.getResources().getDimension(R.dimen.im_width);
        int h = (int)context.getResources().getDimension(R.dimen.im_height);

        String[] projection = new String[]{MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE};

        String selection = null;
        String selectionArgs[] = null;
        String sort = MediaStore.Images.Media.DATE_ADDED;
        Cursor cursor = null;

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            Bundle queryArgs = new Bundle();

            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs
            );

            //sort
            queryArgs.putString(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS, sort
            );

            queryArgs.putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
            );

            // limit, offset
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, limit);
            queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, offSet);

            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sort + " ASC + LIMIT " + String.valueOf(limit) + "OFFSET " +
                    String.valueOf(offSet));
        }

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
        int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

        while(cursor.moveToNext()){
            // Get values of columns for a given image.
            long id = cursor.getLong(idColumn);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            String name = cursor.getString(nameColumn);
            int dateAdded = cursor.getInt(dateAddedColumn);
            int size = cursor.getInt(sizeColumn);
            Bitmap thumb = Util.getBitmap(context, contentUri, w,h);

            imageDataList.add(new ImageData(contentUri, thumb, name, new Date(dateAdded*1000L), size));

        }
        return imageDataList;
    }
}
