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
import com.runvision.util.SPUtil;

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


    //人证比对
    public int faceComperFrame(final Bitmap cardBmp) {
        long start = System.currentTimeMillis();
        int sorce = 0;
        int num = 1;
        boolean flag = false;
        FaceInfo faceInfoKj = null;
        Bitmap bmp = null;
        while (num <= Const.MAX_COMPER_NUM) {
            if (num == 1) {
                //做提取身份证
                byte[] BRG24Kj_card = null;
                BRG24Kj_card = bitmapToBGR24(cardBmp);
                FaceInfo faceInfoKj_card = MyApplication.mDetect.getFacePositionScaleFromGray(BRG24Kj_card, cardBmp.getWidth(), cardBmp.getHeight(), 5);
                card_frature = MyApplication.mFaceFeature.getFaceFeature(BRG24Kj_card, faceInfoKj_card.getFacePosData(0), Const.CARD_WIDTH, Const.CARD_HEIGTH);
            }
            byte[] faceData = MyCameraSuf.getCameraData();
            byte[] redData = MyCameraSuf.getmRedCameraData();

            bmp = getBitMap(faceData);
            byte[] BRG24Kj = null;
            BRG24Kj = MyApplication.mImgUtil.encodeYuv420pToBGR(faceData, Const.PRE_WIDTH, Const.PRE_HEIGTH);
            faceInfoKj = MyApplication.mDetect.faceDetectScale(BRG24Kj, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 5);
            if (faceInfoKj.getRet() != 1) {
                num++;
                continue;
            }

            if (SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE)) {
                byte[] BRG24Kj_LIVE = MyApplication.mImgUtil.encodeYuv420pToBGR(redData, Const.PRE_WIDTH, Const.PRE_HEIGTH);
                FaceInfo faceInfoKj_LIVE = MyApplication.mDetect.faceDetectScale(BRG24Kj_LIVE, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 5);
                if (faceInfoKj_LIVE.getRet() != 1) {
                    num++;
                    continue;
                }
                int ret = MyApplication.mLive.getFaceLive(BRG24Kj, BRG24Kj_LIVE, Const.PRE_WIDTH, Const.PRE_HEIGTH, faceInfoKj.getFacePosData(0), faceInfoKj_LIVE.getFacePosData(0), 30);
                if (ret != 1) {
                    System.out.println("live no");
                    num++;
                    continue;
                }
            }


            //提取模版了
            face_frature = MyApplication.mFaceFeature.getFaceFeature(BRG24Kj, faceInfoKj.getFacePosData(0), Const.PRE_WIDTH, Const.PRE_HEIGTH);
            if (face_frature == null) {
                num = num + 5;
                continue;
            }
            sorce = MyApplication.mFaceFeature.compareFeature(face_frature, card_frature);
            flag = true;
            if (sorce < SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE)) {
                num = num + 5;
                continue;
            } else {
                break;
            }

        }
        // 通过，不需要进行下一次比对
        if (flag && faceInfoKj.getFacePos().length > 0) {
            Rect rect = faceInfoKj.getFacePos(0).getFace();
            AppData.getAppData().setFaceBmp(getFaceImgByInfraredJpg(rect.left, rect.top, rect.right, rect.bottom, bmp));
        } else {
            AppData.getAppData().setFaceBmp(null);
        }
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


    public Bitmap getFaceImgByInfraredJpg(int left, int top, int right, int bottom, Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if (top != bottom && left != right) {
            int iFaceWidth = (int) ((right - left) * 1.5);
            if (iFaceWidth >= width) {
                iFaceWidth = width - 10;
            }

            int iFaceHeight = (int) ((bottom - top) * 1.5);
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
