package com.example.leagueapp1.core.data.local

import androidx.room.TypeConverter
import com.example.leagueapp1.core.domain.models.update.UpdateEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

    @TypeConverter
    fun eventListToString(list: MutableList<UpdateEvent>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToEventList(value: String?): MutableList<UpdateEvent>? {
        if(value == null){
            return mutableListOf()
        }
        val listType = object:
            TypeToken<MutableList<UpdateEvent>?>() {}.type
        return Gson()
            .fromJson(value, listType)
    }
}