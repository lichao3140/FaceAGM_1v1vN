package com.runvision.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/7/21.
 */

public class FileUtils {
    public static void deleteTempter(String imageID, String path) {
        String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/" + path + "/" + imageID + ".jpg";
        File file=new File(sdCardDir);
        if(file.exists()){
           file.delete();
        }
    }


    public static File saveFile(Bitmap btImage, String fileName, String DirName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/";
            if (!DirName.equals("")) {
                sdCardDir = sdCardDir + DirName + "/";
            }
            File dirFile = new File(sdCardDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file = new File(sdCardDir, fileName + ".jpg");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                btImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

    public static Bitmap loadTempBitmap(String fileName, String DirName){
        String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/" + DirName + "/" + fileName + ".jpg";
        File file=new File(sdCardDir);
        if(!file.exists()){
          return null;
        }
        return getSmallBitmap(sdCardDir);
    }

    private static Bitmap getSmallBitmap(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path,options);

        int tempWidth = options.outWidth;
        int tempHeight = options.outHeight;

        while (tempWidth > 1024 || tempHeight > 1024) {
            tempWidth /= 2;
            tempHeight /= 2;
        }
        options.inSampleSize = calculateInSampleSize(options, tempWidth, tempHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path,options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static BitmapFactory.Options getBitmapOption(int inSampleSize) {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    /**
     * 把身份证的图片变成算法需要的BGR24
     *
     * @param bmp
     * @return
     */
    public static byte[] bitmapToBGR24(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte[] value = null;

        int[] pixels = new int[width * height];
        // 获取RGB32数据
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] BGR24 = new byte[width * height * 3];
        //byte[] gray = new byte[width * height];
        // 获取图片的RGB24数据和灰度图数据
        for (int i = 0; i < width * height; i++) {
            int r = (pixels[i] >> 16) & 0x000000FF;
            int g = (pixels[i] >> 8) & 0x000000FF;
            int b = pixels[i] & 0x000000FF;
            BGR24[i * 3] = (byte) (b & 0xFF);
            BGR24[i * 3 + 1] = (byte) (g & 0xFF);
            BGR24[i * 3 + 2] = (byte) (r & 0xFF);
        }

        return BGR24;

    }


}
