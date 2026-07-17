package com.zhengde.chronicles.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhengde.chronicles.game.world.ProvinceState
import com.zhengde.chronicles.game.world.ActiveEvent
import com.zhengde.chronicles.game.world.ChangeEntry

/**
 * Room 类型转换器 — 将复杂类型序列化为 JSON 字符串
 */
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, Int>?): String {
        return gson.toJson(value ?: emptyMap<String, Int>())
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromBooleanMap(value: Map<String, Boolean>?): String {
        return gson.toJson(value ?: emptyMap<String, Boolean>())
    }

    @TypeConverter
    fun toBooleanMap(value: String): Map<String, Boolean> {
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromProvinceMap(value: Map<String, ProvinceState>?): String {
        return gson.toJson(value ?: emptyMap<String, ProvinceState>())
    }

    @TypeConverter
    fun toProvinceMap(value: String): Map<String, ProvinceState> {
        val type = object : TypeToken<Map<String, ProvinceState>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromActiveEventList(value: List<ActiveEvent>?): String {
        return gson.toJson(value ?: emptyList<ActiveEvent>())
    }

    @TypeConverter
    fun toActiveEventList(value: String): List<ActiveEvent> {
        val type = object : TypeToken<List<ActiveEvent>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromChangeEntryList(value: List<ChangeEntry>?): String {
        return gson.toJson(value ?: emptyList<ChangeEntry>())
    }

    @TypeConverter
    fun toChangeEntryList(value: String): List<ChangeEntry> {
        val type = object : TypeToken<List<ChangeEntry>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
