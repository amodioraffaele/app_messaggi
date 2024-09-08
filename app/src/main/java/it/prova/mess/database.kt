package it.prova.mess

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class database(context: Context): SQLiteOpenHelper(context, "database", null,1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE chat (ID_INTERLOCUTORE TEXT PRIMARY KEY, MESSAGGI TEXT, NUMERO TEXT, FOTO TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
    fun inseriscifoto(db: SQLiteDatabase?, foto: String, id: String){
        val da_inserire = android.content.ContentValues().apply {
            put("FOTO", foto)
        }
        db!!.update("chat",da_inserire,"ID_INTERLOCUTORE LIKE ?", arrayOf(id))
    }
  fun prendifoto(db: SQLiteDatabase, id: String): String {
        val cursore = db.rawQuery("SELECT FOTO FROM chat WHERE ID_INTERLOCUTORE LIKE ?", arrayOf(id))
        var ris = ""
        cursore.use {
            ris = it.getString(it.getColumnIndexOrThrow("FOTO"))
        }
        return ris
    }
    fun inserisci(db: SQLiteDatabase?, chat: String, id: String, numero: String){
        val da_inserire = android.content.ContentValues().apply {
            put("MESSAGGI", chat)
            put("ID_INTERLOCUTORE", id)
            put("NUMERO", numero)
        }
        db!!.insertWithOnConflict("chat", null, da_inserire,SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun prendi(db: SQLiteDatabase?, id: String): String{
        var messaggio : String
        try {
            val cursore = db!!.rawQuery("SELECT MESSAGGI FROM chat WHERE ID_INTERLOCUTORE LIKE ?",
            arrayOf(id)
        )
            cursore.use {
                messaggio = it.getString(it.getColumnIndexOrThrow("MESSAGGI"))
            }
        } catch (e: Exception){
            messaggio = ""
        }
        return messaggio
    }
    fun cancella(db: SQLiteDatabase?){
        val cursore = db!!.rawQuery("DROP TABLE chat", null)
        cursore.use {  }
    }

}