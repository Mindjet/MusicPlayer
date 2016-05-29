package com.mindjet.com.musicplayer.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.mindjet.com.musicplayer.ItemBean.Mp3Info;
import com.mindjet.com.musicplayer.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mindjet on 2016/5/19.
 */
public class MediaUtil {

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;

    public static List<Mp3Info> getMp3InfoList(Context context) {

        List<Mp3Info> mp3InfoList = new ArrayList<Mp3Info>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore
                .Audio.Media.DEFAULT_SORT_ORDER);

        for (int i = 0; i < cursor.getCount(); i++) {

            Mp3Info mp3Info = new Mp3Info();

            cursor.moveToNext();

            mp3Info.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mp3Info.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            mp3Info.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            mp3Info.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            mp3Info.year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
            mp3Info.album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            mp3Info.album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            mp3Info.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            mp3Info.url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            mp3Info.isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

            if (mp3Info.isMusic==1&&mp3Info.duration>5000)
                mp3InfoList.add(mp3Info);

        }

        return mp3InfoList;

    }

    //convert time from millisecond to minute:second
    public static String formatTime(long duration) {

        long second = duration / 1000;
        long minute = second / 60;
        second = second % 60;

        String min = String.valueOf(minute);
        String sec = String.valueOf(second);

        if (minute<10) {
            min = "0" + String.valueOf(minute);
        }

        if (second<10){
            sec = "0" + String.valueOf(second);
        }

        return min+":"+sec;

    }

    public static Bitmap getAlbum(Context context, long song_id, long album_id,
                                    boolean allowdefault) {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte [] art = null;
        String path = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.raw.appicon2), null, opts);
    }


}
