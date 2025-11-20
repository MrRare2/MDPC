package dev.mr2.dpc

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 5) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DHIZUKU_CLIENTS)
        db.execSQL(SECURITY_LOGS)
        db.execSQL(NETWORK_LOGS)
        db.execSQL(APP_GROUPS)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL(SECURITY_LOGS)
        if (oldVersion < 3) db.execSQL(NETWORK_LOGS)
        if (oldVersion < 5) db.execSQL(APP_GROUPS)
    }
    companion object {
        // ver 1
        const val DHIZUKU_CLIENTS = "CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY," +
                "signature TEXT, permissions TEXT)"
        // ver 2
        const val SECURITY_LOGS = "CREATE TABLE security_logs (id INTEGER, tag INTEGER," +
                "level INTEGER, time INTEGER, data TEXT)"
        // ver 3
        const val NETWORK_LOGS = "CREATE TABLE network_logs (id INTEGER, package INTEGER," +
                "time INTEGER, type TEXT, host TEXT, count INTEGER, addresses TEXT," +
                "address TEXT, port INTEGER)"
        // ver 4 (refactor)
        // ver 5 + refactor again
        const val APP_GROUPS = "CREATE TABLE app_groups(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, apps TEXT)"
    }
}
