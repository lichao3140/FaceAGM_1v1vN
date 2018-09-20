package com.face.sv;

/**
 * Created by Administrator on 2018/7/26.
 */

public class FaceFeature {
    private final static String TAG = "FaceRecognize";
    private FaceFeatureNative FaceFeature;
    private byte[] mutexFeature = new byte[0];
    private byte[] mutexCompare = new byte[0];

    public FaceFeature() {
        FaceFeature = FaceFeatureNative.getInstance();
    }


    public byte[] getFaceFeature(byte[] BGR24, byte[] faceInfo, int width, int height) {
        byte[] feature = null;
        synchronized (mutexFeature) {
            feature = FaceFeature.getFaceFeature(BGR24, faceInfo, width, height);
        }
        return feature;
    }


    public int compareFeature(byte[] feature1, byte[] feature2) {
        int socre = 0;
        synchronized (mutexFeature) {
            socre = FaceFeature.compareFeature(feature1, feature2);
        }
        return socre;
    }
}
