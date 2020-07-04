package com.uicode.smallchat.smallchatserver.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.uicode.smallchat.smallchatserver.model.channel.ChannelMessage.MessageCode;
import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.service.BackgroundService;
import com.uicode.smallchat.smallchatserver.service.ChannelService;
import com.uicode.smallchat.smallchatserver.service.ChatStateService;

import io.vertx.core.Vertx;

public class BackgroundServiceImpl implements BackgroundService {

    private static final Logger LOGGER = LogManager.getLogger(BackgroundServiceImpl.class);

    private static final Integer INIT_DELAY_MS = 1000;
    private static final String TEST_CHANNEL_ID = "test";
    private static final String WELCOME_CHANNEL_ID = "welcome";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd / MM / yyyy");
    private static final String SCHEDULED_MESSAGE = "Automatic message at %s";

    private final Vertx vertx;
    private final ChannelService channelService;
    private final ChatStateService chatStateService;

    @Inject
    public BackgroundServiceImpl(Vertx vertx, ChannelService channelService, ChatStateService chatStateService) {
        this.vertx = vertx;
        this.channelService = channelService;
        this.chatStateService = chatStateService;
    }

    @Override
    public void init() {
        vertx.setTimer(INIT_DELAY_MS, delay -> {
            this.createDefaultChannels();
            this.launchScheduledSender();
        });
    }

    private void createDefaultChannels() {
        chatStateService.getChatState().future().onComplete(chatStateResult -> {
            if (chatStateResult.failed()) {
                LOGGER.error("Error while getting ChatState", chatStateResult.cause());
                return;
            }

            if (!isChannelExist(chatStateResult.result(), TEST_CHANNEL_ID)) {
                Channel testChannel = new Channel();
                testChannel.setId(TEST_CHANNEL_ID);
                testChannel.setName("Test");
                testChannel.setDescription("Channel for tests");
                chatStateService.createChannel(testChannel);
            }
            if (!isChannelExist(chatStateResult.result(), WELCOME_CHANNEL_ID)) {
                Channel welcomeChannel = new Channel();
                welcomeChannel.setId(WELCOME_CHANNEL_ID);
                welcomeChannel.setName("Welcome");
                welcomeChannel.setDescription("Welcome on SmallChat");
                chatStateService.createChannel(welcomeChannel);
            }
        });
    }

    public void launchScheduledSender() {
        LocalDateTime nextTimeRaw = LocalDateTime.now();
        LocalDateTime nextTimeAdjusted = LocalDateTime.of(nextTimeRaw.getYear(), nextTimeRaw.getMonth(),
                nextTimeRaw.getDayOfMonth(), 0, 0, 1);
        if (nextTimeAdjusted.getDayOfMonth() == nextTimeRaw.getDayOfMonth()) {
            nextTimeAdjusted = nextTimeAdjusted.plusDays(1);
        }

        vertx.setTimer(Duration.between(LocalDateTime.now(), nextTimeAdjusted).getSeconds() * 1000, delay -> {
            LocalDateTime currentTime = LocalDateTime.now();
            String message = String.format(SCHEDULED_MESSAGE, currentTime.format(DATE_TIME_FORMAT));
            channelService.sendServerMessage(TEST_CHANNEL_ID, message, MessageCode.MSG);

            this.launchScheduledSender();
        });
    }

    private boolean isChannelExist(ChatState chatState, String channelId) {
        return chatState.getChannels().stream().anyMatch(channel -> channel.getId().equals(channelId));
    }

}
