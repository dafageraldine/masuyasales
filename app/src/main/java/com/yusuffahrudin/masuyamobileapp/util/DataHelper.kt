package com.yusuffahrudin.masuyamobileapp.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.yusuffahrudin.masuyamobileapp.data.UserAkses

class DataHelper (context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private lateinit var db: SQLiteDatabase

    companion object {
        private val DATABASE_NAME = "UserDB"
        private val DATABASE_VERSION = 1
        private val TABLE_NAME = "UserAkses"
        private val ID = "id"
        private val MODUL = "Modul"
        private val AKSES = "Akses"
        private val TAMBAH = "Tambah"
        private val UBAH = "Ubah"
        private val HAPUS = "Hapus"
        private val POST = "Post"
        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        // TODO Auto-generated method stub
        val sql = "CREATE TABLE $TABLE_NAME($ID INTEGER PRIMARY KEY, $MODUL TEXT," +
                "$AKSES INTEGER, $TAMBAH INTEGER, $UBAH INTEGER, $HAPUS INTEGER, $POST INTEGER)"
        Log.d("Data", "onCreate: $sql")
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    //Inserting (Creating) data
    fun addUser(userAkses: UserAkses): Boolean {
        val sql = "INSERT INTO $TABLE_NAME ($MODUL,$AKSES,$TAMBAH,$UBAH,$HAPUS,$POST) VALUES (?,?,?,?,?,?)"
        val stmt = db.compileStatement(sql)
        stmt.bindString(1, userAkses.modul)
        stmt.bindString(2, userAkses.akses.toString())
        stmt.bindString(3, userAkses.add.toString())
        stmt.bindString(4, userAkses.edit.toString())
        stmt.bindString(5, userAkses.delete.toString())
        stmt.bindString(6, userAkses.post.toString())
        val _success = stmt.executeInsert()
        stmt.clearBindings()
        return (Integer.parseInt("$_success") != -1)
    }

    fun getAllData(): ArrayList<UserAkses> {
        val userAksesList: ArrayList<UserAkses>
        userAksesList = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val database = this.writableDatabase
        val cursor = database.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val user = UserAkses()
                user.modul = cursor.getString(cursor.getColumnIndex(MODUL))
                user.akses = cursor.getInt(cursor.getColumnIndex(AKSES))
                user.add = cursor.getInt(cursor.getColumnIndex(TAMBAH))
                user.edit = cursor.getInt(cursor.getColumnIndex(UBAH))
                user.delete = cursor.getInt(cursor.getColumnIndex(HAPUS))
                user.post = cursor.getInt(cursor.getColumnIndex(POST))
                userAksesList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        database.close()
        return userAksesList
    }

    fun insertDB(){
        db = this.writableDatabase
        db.beginTransaction()
    }

    fun setTransactionSuccess() {
        db.setTransactionSuccessful()
    }

    fun closeDB(){
        db.endTransaction()
    }

    fun deleteUser(): Boolean{
        db = this.writableDatabase
        val _success = db.delete(TABLE_NAME, null, null)
        db.close()
        return (Integer.parseInt("$_success") != -1)
    }
}