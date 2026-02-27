package com.example.loan_origination_system.config;

import com.example.loan_origination_system.bot.PawnShopTelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(PawnShopTelegramBot pawnShopTelegramBot) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(pawnShopTelegramBot);
            System.out.println("✅ Telegram Bot successfully connected!");
            return api;
        } catch (TelegramApiException e) {
            System.err.println("❌ Failed to connect: " + e.getMessage());
            return null;
        }
    }
}