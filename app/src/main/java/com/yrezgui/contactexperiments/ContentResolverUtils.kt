package com.yrezgui.contactexperiments

import android.database.Cursor
import androidx.core.database.getStringOrNull

fun Cursor.getAllData(): Map<String, String?> {
    return columnNames.associateWith {
        getStringOrNull(
            getColumnIndex(it)
        )
    }.toSortedMap()
}