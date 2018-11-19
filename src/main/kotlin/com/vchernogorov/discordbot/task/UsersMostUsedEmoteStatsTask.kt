package com.vchernogorov.discordbot.task

import com.vchernogorov.discordbot.UserMessage
import com.vchernogorov.discordbot.UserStatsArgs
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UsersMostUsedEmoteStatsTask : MessagesStatsTask() {

    override fun execute(event: MessageReceivedEvent, args: UserStatsArgs) {
        val userMostUsedEmoteStatsTask = UserMostUsedEmoteStatsTask()
        val filteredMembers = args.members.map {
            val messagesCount = transaction {
                val result = UserMessage.slice(UserMessage.id).select {
                    UserMessage.creatorId.eq(it.user.id) and
                            (UserMessage.channelId.inList((if (args.channels.isNotEmpty()) args.channels else event.guild.textChannels).map { it.id }))
                }
                result.count()
            }
            it to messagesCount
        }.filter { (_, count) ->
            count >= args.limitUsersByMessages
        }.sortedByDescending { (_, count) ->
            count
        }.take(args.limitPrimaryResults).map { (member, _) ->
            member
        }

        filteredMembers.forEach {
            userMostUsedEmoteStatsTask.execute(event, it, args.limitSecondaryResults, args.channels)
        }
    }
}