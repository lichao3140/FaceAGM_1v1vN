package com.runvision.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;

import com.face.sv.FaceFeatureNative;
import com.face.sv.FaceInfo;
import com.runvision.bean.AppData;
import com.runvision.myview.MyCameraSuf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FaceIDCardCompareLib {
    private String TAG = "FaceIDCardCompareLib";

    private Context mContext;
    private static FaceIDCardCompareLib faceCore = null;


    public synchronized static FaceIDCardCompareLib getInstance() {
        return faceCore == null ? (faceCore = new FaceIDCardCompareLib()) : faceCore;
    }


    private byte[] card_frature = null;
    private byte[] face_frature = null;
    private boolean card_status = false;
    private int sorce = 0;

    //人证比对
    public int faceComperFrame(final Bitmap cardBmp) {
        long start = System.currentTimeMillis();
        sorce = 0;
        int num = 1;
        card_status = false;
        while (num <= Const.MAX_COMPER_NUM) {
            if (num == 1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] BRG24Kj_card = null;
                        BRG24Kj_card = bitmapToBGR24(cardBmp);
                        FaceInfo faceInfoKj_card = MyApplication.mDetect.getFacePositionScaleFromGray(BRG24Kj_card, cardBmp.getWidth(), cardBmp.getHeight(), 5);
                        System.out.println("card ret:"+faceInfoKj_card.getRet());
                        card_frature = FaceFeatureNative.getFaceFeature(BRG24Kj_card, faceInfoKj_card.getFacePosData(0), Const.CARD_WIDTH, Const.CARD_HEIGTH);
                        if(card_frature==null){
                            System.out.println("card null");
                        }
                        card_status = true;
                    }
                }).start();
            }

            byte[] faceData = MyCameraSuf.getCameraData();
            Bitmap bmp = getBitMap(faceData);
            byte[] BRG24Kj = null;
            BRG24Kj = MyApplication.mImgUtil.encodeYuv420pToBGR(faceData, Const.PRE_WIDTH, Const.PRE_HEIGTH);
            FaceInfo faceInfoKj = MyApplication.mDetect.faceDetectScale(BRG24Kj, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 5);
            //System.out.println("单目人脸检测 Time:" + (System.currentTimeMillis() - start));
            if (faceInfoKj.getRet() != 1) {
                //无人脸
                System.out.println("no face");
                num++;
                continue;
            }
            //做活体
            byte[] redData = MyCameraSuf.getmRedCameraData();
            byte[] BRG24Kj_LIVE = MyApplication.mImgUtil.encodeYuv420pToBGR(redData, Const.PRE_WIDTH, Const.PRE_HEIGTH);
            FaceInfo faceInfoKj_LIVE = MyApplication.mDetect.faceDetectScale(BRG24Kj_LIVE, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 5);
            //System.out.println("活体检测 Time:" + (System.currentTimeMillis() - start));
            if (faceInfoKj_LIVE.getRet() != 1) {
                //活体检测无人脸
                System.out.println("live no face");
                num++;
                continue;
            }
            int ret = MyApplication.mLive.getFaceLive(BRG24Kj, BRG24Kj_LIVE, Const.PRE_WIDTH, Const.PRE_HEIGTH, faceInfoKj.getFacePosData(0), faceInfoKj_LIVE.getFacePosData(0), 30);
            //System.out.println("活体识别 Time:" + (System.currentTimeMillis() - start));
            if (ret != 1) {
                //不是活体
                System.out.println("live no");
                num = num + 4;
                continue;
            }
            //提取模版了
            face_frature = FaceFeatureNative.getFaceFeature(BRG24Kj, faceInfoKj.getFacePosData(0), Const.PRE_WIDTH, Const.PRE_HEIGTH);
            //System.out.println("提取模版 timne:" + (System.currentTimeMillis() - start));
            if (face_frature == null) {
                System.out.println("getTempter error");
                num = num + 8;
                continue;
            }
            boolean flagComper = true;
            while (true) {
                if (card_status) {
                    sorce = FaceFeatureNative.compareFeature(face_frature, card_frature);
                    // System.out.println(sorce);
                    if (sorce < Const.FACE_SCORE) {
                        System.out.println("分数不够：" + sorce);
                        num = num + 8;
                        flagComper = true;

                        if (sorce == 0) {
                            sorce++;
                        }

                        break;
                    } else {
                        // 通过，不需要进行下一次比对
                        flagComper = false;
                        break;
                    }
                }
            }
            // System.out.println("Comper Time:" + (System.currentTimeMillis() - start));
            Rect rect = faceInfoKj.getFacePos(0).getFace();
            AppData.getAppData().setFaceBmp(getFaceImgByInfraredJpg(rect.left, rect.top, rect.right, rect.bottom, bmp));

            if (flagComper) {
                continue;
            } else {
                break;
            }
        }
        // System.out.println("比对:" + (System.currentTimeMillis() - start));
        return sorce;

    }

    /**
     * 把身份证的图片变成算法需要的BGR24
     *
     * @param bmp
     * @return
     */
    public byte[] bitmapToBGR24(Bitmap bmp) {
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

    public Bitmap getBitMap(byte[] data) {
        final YuvImage image = new YuvImage(data, ImageFormat.NV21, 640, 480, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        if (!image.compressToJpeg(new Rect(0, 0, 640, 480), 100, os)) {
            return null;
        }
        byte[] tmp = os.toByteArray();
        Bitmap mapLbb = getSmallBitmap(tmp);
        return mapLbb;
    }


    public Bitmap getSmallBitmap(byte[] aray) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(aray, 0, aray.length, options);

        int tempWidth = options.outWidth;
        int tempHeight = options.outHeight;

        while (tempWidth > 1024 || tempHeight > 1024) {
            tempWidth /= 2;
            tempHeight /= 2;
        }
        options.inSampleSize = calculateInSampleSize(options, tempWidth, tempHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(aray, 0, aray.length, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    public void getGrayDataFromRgb32(int[] srcImgData, int nWidth, int nHeight, byte[] pbGrayData) {
        Bitmap srcColor = Bitmap.createBitmap(srcImgData, nWidth, nHeight, Bitmap.Config.ARGB_8888);
        int w = srcColor.getWidth(), h = srcColor.getHeight();
        int[] pix = new int[w * h];
        srcColor.getPixels(pix, 0, w, 0, 0, w, h);

        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = pix[w * i + j];
                int red = ((color & 0x00FF0000) >> 16);
                int green = ((color & 0x0000FF00) >> 8);
                int blue = color & 0x000000FF;
                color = (red + green + blue) / 3;
                color = alpha | (color << 16) | (color << 8) | color;
                pbGrayData[w * i + j] = (byte) color;
            }
        }
        pix = null;
        if (!srcColor.isRecycled()) {
            srcColor.recycle();
        }
        System.gc();
        System.gc();

    }

    public File saveFile(Bitmap btImage, String fileName, String path) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/";
            if (!path.equals("")) {
                sdCardDir = sdCardDir + path + "/";
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

    public Bitmap getFaceImgByInfraredJpg(int left, int top, int right, int bottom, Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if (top != bottom && left != right) {
            int iFaceWidth = (right - left) * 2;
            if (iFaceWidth >= width) {
                iFaceWidth = width - 10;
            }

            int iFaceHeight = (bottom - top) * 3;
            if (iFaceHeight >= height) {
                iFaceHeight = height - 10;
            }

            int iLeft = left + (right - left) / 2 - iFaceWidth / 2;
            iLeft = iLeft > 0 ? iLeft : 0;

            int iTop = top + (bottom - top) / 2 - iFaceHeight / 2;
            iTop = iTop > 0 ? iTop : 0;

            if (iLeft < width && iTop < height) {
                int iWidth = 0;
                int iHeight = 0;
                if (width < (iLeft + iFaceWidth)) {
                    iWidth = width - iLeft - 10;
                } else {
                    iWidth = iFaceWidth;
                }

                if (height < (iTop + iFaceHeight)) {
                    iHeight = height - iTop - 10;
                } else {
                    iHeight = iFaceHeight;
                }

                int oldW = iWidth;
                iWidth = (int) ((81.0f / 111.0f) * (float) iHeight);
                iLeft = iLeft + ((oldW / 2) - iWidth / 2);
                iLeft = iLeft > 0 ? iLeft : 0;

                if (iLeft + iWidth >= bmp.getWidth()) {
                    iWidth = bmp.getWidth() - iLeft - 5;
                }
                return Bitmap.createBitmap(bmp, iLeft, iTop, iWidth, iHeight);
            }
        }
        return null;
    }

}
