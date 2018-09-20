package com.runvision.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class FaceProvider {
    private final static String TAG = "FaceProvider";
    private final static String FACE_DB = "FaceTemplate.db";
    private FaceDataHelper faceHelper;

    public FaceProvider(Context context) {
        faceHelper = new FaceDataHelper(context, FACE_DB, null, 1);
    }

    /**
     * 添加用户记录
     *
     * @param user 用户信息
     * @return 添加记录的id号, 小于0表示失败。
     */
    public synchronized int addUserOutId(User user) {
        int ret = -1;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("insert into tUser (userID,userName, userType, createTime,imageId) values(?,?,?,?,?)",
                        new Object[]{
                                user.getUserid(),
                                user.getName(),
                                user.getType(),
                                user.getTime(),
                                user.getImagepath()
                        });
                Cursor cursor = db.rawQuery("select last_insert_rowid() from tUser", null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        ret = cursor.getInt(0);
                    } else {
                        ret = -4;
                    }
                    cursor.close();
                } else {
                    ret = -3;
                }
            } else {
                ret = -2;
            }
        } else {
            ret = -1;
        }
        return ret;
    }

    public int updateUserById(User user){
        if(null!=faceHelper){
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                db.execSQL("update tUser set userId= ?, userName = ?, userType = ? where id = ?",
                        new Object[]{
                                user.getUserid(),
                                user.getName(),
                                user.getType(),
                                user.getId()
                        });
                return 0;
            } else {
                return -2;
            }
        }else {
            return -1;
        }
    }

    public int deleteUserById(int id) {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("delete from tUser where id =" + id);
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public int deleteAllUser() {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("delete from tUser");
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }
    }

    public User getUserByUserId(int userId) {
        User user = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser where id = " + userId;
                Cursor cursor = db.rawQuery( sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    user.setName(cursor.getString(cursor.getColumnIndex("userName")));
                    user.setUserid(cursor.getString(cursor.getColumnIndex("userID")));
                    user.setType(cursor.getString(cursor.getColumnIndex("userType")));
                    user.setTime(cursor.getString(cursor.getColumnIndex("createTime")));
                    user.setImagepath(cursor.getString(cursor.getColumnIndex("imageId")));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return user;
    }

    public User getUserById(int id) {
        User user = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser where id = " + id;
                Cursor cursor = db.rawQuery( sql, null);
                if (null != cursor && cursor.getCount() > 0) {
                    user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    user.setName(cursor.getString(cursor.getColumnIndex("userName")));
                    user.setUserid(cursor.getString(cursor.getColumnIndex("userID")));
                    user.setType(cursor.getString(cursor.getColumnIndex("userType")));
                    user.setTime(cursor.getString(cursor.getColumnIndex("createTime")));
                    user.setImagepath(cursor.getString(cursor.getColumnIndex("imageId")));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return user;
    }

    public List<User> queryUsers() {
        List<User> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser order by id asc";
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<User>();
                    User user;
                    while (cursor.moveToNext()) {
                        user = new User();
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));

                        user.setName(cursor.getString(cursor.getColumnIndex("userName")));
                        user.setUserid(cursor.getString(cursor.getColumnIndex("userID")));
                        user.setType(cursor.getString(cursor.getColumnIndex("userType")));
                        user.setTime(cursor.getString(cursor.getColumnIndex("createTime")));
                        user.setImagepath(cursor.getString(cursor.getColumnIndex("imageId")));
                        list.add(user);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    public List<User> queryUsers(int limit) {
        List<User> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tUser order by id desc limit 18 offset " + limit;
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<User>();
                    User user;
                    while (cursor.moveToNext()) {
                        user = new User();
                        user.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        user.setName(cursor.getString(cursor.getColumnIndex("userName")));
                        user.setUserid(cursor.getString(cursor.getColumnIndex("userID")));
                        user.setType(cursor.getString(cursor.getColumnIndex("userType")));
                        user.setTime(cursor.getString(cursor.getColumnIndex("createTime")));
                        user.setImagepath(cursor.getString(cursor.getColumnIndex("imageId")));
                        list.add(user);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 查询数据库中的总条数.
     *
     * @return
     */
    public int quaryUserTableRowCount() {
        int count = 0;
        String sql = "select count(id) from tUser";
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }


    /**
     * 查询数据库中的总条数.
     *
     * @return
     */
    public int quaryRecordTableRowCount() {
        int count = 0;
        String sql = "select count(id) from tRecord";
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }

    public List<Record> queryRecord(int pageSize,int limit) {
        List<Record> list = null;
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                String sql = "select * from tRecord order by id desc limit "+pageSize+" offset " + limit;
                Cursor cursor = db.rawQuery(sql, null);
                if (null != cursor) {
                    list = new ArrayList<Record>();
                    Record record;
                    while (cursor.moveToNext()) {
                        record = new Record();
                        record.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        record.setName(cursor.getString(cursor.getColumnIndex("userName")));
                        record.setUserid(cursor.getString(cursor.getColumnIndex("userID")));
                        record.setType(cursor.getString(cursor.getColumnIndex("userType")));
                        record.setTime(cursor.getString(cursor.getColumnIndex("createTime")));
                        record.setScore(cursor.getString(cursor.getColumnIndex("sorce")));
                        record.setCompertResult(cursor.getString(cursor.getColumnIndex("comperResult")));
                        record.setSnapImageID(cursor.getString(cursor.getColumnIndex("snapImageID")));
                        record.setTemplateImageID(cursor.getString(cursor.getColumnIndex("templateImageID")));
                        list.add(record);
                    }
                    cursor.close();
                }
            }
        }
        return list;
    }

    /**
     * 添加用户记录
     *
     * @param record 用户信息
     * @return 添加记录的id号, 小于0表示失败。
     */
    public int addRecord(Record record) {
        if (null != faceHelper) {
            SQLiteDatabase db = faceHelper.getWritableDatabase();
            if (null != db) {
                //userName, identityNo, identityImg, cameraImg, similarity, createTime
                db.execSQL("insert into tRecord (userID,userName, userType,sorce,comperResult,createTime,snapImageID,templateImageID) values(?,?,?,?,?,?,?,?)",
                        new Object[]{
                                record.getUserid(),
                                record.getName(),
                                record.getType(),
                                record.getScore(),
                                record.getCompertResult(),
                                record.getTime(),
                                record.getSnapImageID(),
                                record.getTemplateImageID()
                        });
                return 0;
            } else {
                return -2;
            }
        } else {
            return -1;
        }

    }

    private void log(String msg) {
        Log.e(TAG, msg);
    }
}
