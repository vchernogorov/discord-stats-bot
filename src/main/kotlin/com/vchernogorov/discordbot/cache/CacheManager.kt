package com.vchernogorov.discordbot.cache

import com.vchernogorov.discordbot.args.GuildStatsArgs
import com.vchernogorov.discordbot.args.MemberStatsArgs
import com.vchernogorov.discordbot.args.StringOccurrenceArgs
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import redis.clients.jedis.JedisPool
import java.util.*

/**
 * Cache manager consists of common methods for working with [JedisPool].
 */
class CacheManager(val jedisPool: JedisPool, val cacheExpiration: Int) {

    /**
     * Creates a key from [StringOccurrenceArgs.command], [Guild.id] and [StringOccurrenceArgs.strings].
     * Grabs the value by created key from [JedisPool] and converts it to [T] type using provided [mapper].
     * Returns the value associated with the key or null if no value found by the generated key.
     */
    fun <T> getFromCache(guild: Guild, args: StringOccurrenceArgs, mapper: (String) -> T): T? {
        return jedisPool.resource.use {
            val strings = args.strings.map {
                it.joinToString(separator = "&") }.joinToString(separator = "|")
            val key = "${args.command}/${guild.id}/$strings"
            val value = it.get(key)
            if (value == null) null else mapper.invoke(value)
        }
    }

    /**
     * Creates a key from [GuildStatsArgs.command] and [Guild.id].
     * Grabs the value by created key from [JedisPool] and converts it to [T] type using provided [mapper].
     * Returns the value associated with the key or null if no value found by the generated key.
     */
    fun <T> getFromCache(guild: Guild, args: GuildStatsArgs, mapper: (String) -> T): T? {
        return jedisPool.resource.use {
            val key = "${args.command}/${guild.id}"
            val value = it.get(key)
            if (value == null) null else mapper.invoke(value)
        }
    }

    /**
     * Creates a key from [MemberStatsArgs.command], [Member.getGuild] and [Member.getUser].
     * Grabs the value by created key from [JedisPool] and converts it to [T] type using provided [mapper].
     * Returns the value associated with the key or null if no value found by the generated key.
     */
    fun <T> getFromCache(member: Member, args: MemberStatsArgs, mapper: (String) -> T): T? {
        return jedisPool.resource.use {
            val key = if (args.compare.isEmpty()) {
                "${args.command}/${member.guild.id}/${member.user.id}"
            } else {
                val membersHash = Math.abs(Arrays.hashCode(args.compare.map { it.user.id }.toTypedArray()))
                "${args.command}/${member.guild.id}/${member.user.id}/$membersHash"
            }
            val value = it.get(key)
            if (value == null) null else  mapper.invoke(value)
        }
    }

    /**
     * Creates a key from [MemberStatsArgs.command], [Member.getGuild] and [Member.getUser].
     * Saves converted by [mapper] value to [JedisPool] using created key.
     * Expiration time of the cached value is set by [MyArgs.cacheExpiration] parameter.
     * Returns generated key.
     */
    fun saveToCache(member: Member, args: MemberStatsArgs, value: String): String {
        return jedisPool.resource.use {
            val key = if (args.compare.isEmpty()) {
                "${args.command}/${member.guild.id}/${member.user.id}"
            } else {
                val compareHash = Math.abs(Arrays.hashCode(args.compare.map { it.user.id }.toTypedArray()))
                "${args.command}/${member.guild.id}/${member.user.id}/$compareHash"
            }
            it.set(key, value)
            it.expire(key, cacheExpiration)
            key
        }
    }

    /**
     * Creates a key from [GuildStatsArgs.command] and [Guild.id].
     * Saves converted by [mapper] value to [JedisPool] using created key.
     * Expiration time of the cached value is set by [MyArgs.cacheExpiration] parameter.
     * Returns generated key.
     */
    fun saveToCache(guild: Guild, args: GuildStatsArgs, value: String): String {
        return jedisPool.resource.use {
            val key = "${args.command}/${guild.id}"
            it.set(key, value)
            it.expire(key, cacheExpiration)
            key
        }
    }

    /**
     * Creates a key from [StringOccurrenceArgs.command], [Guild.id] and [StringOccurrenceArgs.strings].
     * Saves converted by [mapper] value to [JedisPool] using created key.
     * Expiration time of the cached value is set by [MyArgs.cacheExpiration] parameter.
     * Returns generated key.
     */
    fun saveToCache(guild: Guild, args: StringOccurrenceArgs, value: String): String {
        return jedisPool.resource.use {
            val strings = args.strings.map {
                it.joinToString(separator = "&") }.joinToString(separator = "|")
            val key = "${args.command}/${guild.id}/$strings"
            it.set(key, value)
            it.expire(key, cacheExpiration)
            key
        }
    }

}