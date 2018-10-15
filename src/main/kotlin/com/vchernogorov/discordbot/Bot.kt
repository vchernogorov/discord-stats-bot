package com.vchernogorov.discordbot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.io.File
import java.util.stream.Collectors

val gson = Gson()
var token: String? = null
val jda: JDA by lazy {
  JDABuilder(AccountType.BOT)
          .addEventListener(OwnerCommandListener())
      .setToken(token ?: throw Exception("Token wasn't populated."))
      .build()
}

fun main(args: Array<String>) {
  println(System.getenv("PORT"))
  token = "MzE3Njg2NDA2MzU1MDkxNDU2.DAwpLw.0dfNNvkX08I0SZ5kGQkNrdn2xv8"
  jda
}

inline fun <reified T> Gson.fromJson(json: String) =
    this.fromJson<T>(json, object: TypeToken<T>() {}.type)

fun MessageChannel.getMessages(limit: Long) =
    this.iterableHistory.stream().limit(limit).collect(Collectors.toList())

fun MessageReceivedEvent.send(message: String) =
    textChannel.sendMessage(net.dv8tion.jda.core.MessageBuilder().append(message).build())

fun File.createEverything(): File {
  parentFile.mkdirs()
  createNewFile()
  return this
}