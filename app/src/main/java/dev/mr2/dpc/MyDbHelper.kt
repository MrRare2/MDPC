package dev.mr2.dpc

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 4) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY," +
            "signature TEXT, permissions TEXT)")
        db.execSQL("CREATE TABLE security_logs (id INTEGER, tag INTEGER, level INTEGER," +
            "time INTEGER, data TEXT)")
        db.execSQL(
            "CREATE TABLE network_logs (id INTEGER, package INTEGER, time INTEGER," +
                "type TEXT, host TEXT, count INTEGER, addresses TEXT, address TEXT," +
                "port INTEGER)"
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // ver 2
        db.execSQL("CREATE TABLE IF NOT EXISTS security_logs (id INTEGER, tag INTEGER, level INTEGER," +
            "time INTEGER, data TEXT)")
        // ver 3
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS network_logs (id INTEGER, package INTEGER, time INTEGER," +
                "type TEXT, host TEXT, count INTEGER, addresses TEXT, address TEXT," +
                "port INTEGER)"
        )
        // ver 4, fix upstream issue #189 (https://github.com/BinTianqi/OwnDroid/issues/189)
    }
}
