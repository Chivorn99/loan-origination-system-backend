package com.example.loan_origination_system.bot;

import com.example.loan_origination_system.exception.BusinessException;
import com.example.loan_origination_system.model.loan.PawnLoan;
import com.example.loan_origination_system.service.PawnLoanService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PawnShopTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final PawnLoanService pawnLoanService;
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    public PawnShopTelegramBot(@Value("${telegram.bot.token}") String botToken, PawnLoanService pawnLoanService) {
        super(botToken);
        this.pawnLoanService = pawnLoanService;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (messageText.equalsIgnoreCase("/start") || messageText.equalsIgnoreCase("/cancel")) {
                userStates.remove(chatId);
                sendMessage(chatId, "🏦 *Welcome to Loy Pawn Shop!*\n\n1️⃣ Check Loan Status\n2️⃣ Pay Bill (Soon)\n\n_Type /cancel to return here._");
                return;
            }

            String state = userStates.getOrDefault(chatId, "NONE");

            if (state.equals("NONE") && messageText.equals("1")) {
                userStates.put(chatId, "WAITING_FOR_CODE");
                sendMessage(chatId, "🔍 Please enter your *Loan Code* (e.g., LOAN-XXXXXX):");
            } else if (state.equals("WAITING_FOR_CODE")) {
                handleStatus(chatId, messageText);
            }
        }
    }

    private void handleStatus(long chatId, String code) {
        try {
            PawnLoan loan = pawnLoanService.getLoanByCode(code);
            String resp = String.format("📄 *Loan Found!*\n🔖 Code: `%s`\n💰 Principal: $%.2f\n📅 Due: %s",
                    loan.getLoanCode(), loan.getLoanAmount(), loan.getDueDate());
            sendMessage(chatId, resp);
            userStates.remove(chatId);
        } catch (BusinessException e) {
            sendMessage(chatId, "❌ " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chatId));
        sm.setText(text);
        sm.setParseMode("Markdown");
        try { execute(sm); } catch (TelegramApiException e) { e.printStackTrace(); }
    }
}