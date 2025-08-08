package com.yrezgui.contactexperiments

import android.database.Cursor
import androidx.core.database.getStringOrNull

fun Cursor.getAllData(): Map<String, String?> {
    val columns = columnNames.also {
        it.sort()
    }

    return columns.associateWith {
        getStringOrNull(
            getColumnIndex(it)
        )
    }
}