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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PawnShopTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    // Direct injection! We bypass the Controller entirely.
    private final PawnLoanService pawnLoanService;

    // This is the Bot's "Memory". It remembers what step of the flow the user is in.
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    public PawnShopTelegramBot(@Value("${telegram.bot.token}") String botToken, PawnLoanService pawnLoanService) {
        super(botToken);
        this.pawnLoanService = pawnLoanService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getChat().getFirstName();

            // 1. Handle universal commands (like /start or /cancel)
            if (messageText.equalsIgnoreCase("/start") || messageText.equalsIgnoreCase("/cancel")) {
                userStates.remove(chatId); // Clear their memory
                String greeting = "🏦 *Welcome to Loy Pawn Shop, " + userFirstName + "!*\n\n" +
                        "How can I help you today? Please reply with a number:\n\n" +
                        "1️⃣ Check Loan Status\n" +
                        "2️⃣ Pay Bill (Coming soon)\n\n" +
                        "_(Type /cancel at any time to return to this menu)_";
                sendMessage(chatId, greeting);
                return; // Stop processing and wait for next message
            }

            // 2. Check the user's current memory state
            String currentState = userStates.getOrDefault(chatId, "NONE");

            // Flow: User clicked "1" from the main menu
            if (currentState.equals("NONE") && messageText.equals("1")) {
                userStates.put(chatId, "WAITING_FOR_LOAN_CODE");
                sendMessage(chatId, "🔍 Please enter your exact *Loan Code*\n(Example: LOAN-123456-ABCDEF)");
            }
            // Flow: User is currently trying to give us a loan code
            else if (currentState.equals("WAITING_FOR_LOAN_CODE")) {
                handleCheckStatusRequest(chatId, messageText);
            }
            // Catch-all for unknown inputs
            else {
                sendMessage(chatId, "I didn't quite understand that. Type /start to see the main menu!");
            }
        }
    }

    // ==========================================
    // Core Business Logic Methods
    // ==========================================

    private void handleCheckStatusRequest(long chatId, String loanCode) {
        try {
            // Talk directly to your service layer!
            PawnLoan loan = pawnLoanService.getLoanByCode(loanCode);

            // Format the dates and amounts beautifully
            String statusIcon = loan.getStatus().name().equals("ACTIVE") ? "🟢" :
                    loan.getStatus().name().equals("DEFAULTED") ? "🔴" : "⚪";

            long daysUntilDue = LocalDate.now().until(loan.getDueDate(), ChronoUnit.DAYS);
            String dueWarning = daysUntilDue < 0 ? "\n⚠️ *WARNING: OVERDUE by " + Math.abs(daysUntilDue) + " days!*" : "";

            String response = String.format(
                    "📄 *Loan Details Found!*\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "🔖 *Code:* `%s`\n" +
                            "%s *Status:* %s\n" +
                            "💰 *Principal Amount:* $%.2f\n" +
                            "💵 *Total to Pay:* $%.2f\n" +
                            "📅 *Due Date:* %s%s\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "Type /start to return to the main menu.",
                    loan.getLoanCode(),
                    statusIcon, loan.getStatus(),
                    loan.getLoanAmount(),
                    loan.getTotalPayableAmount(),
                    loan.getDueDate(),
                    dueWarning
            );

            sendMessage(chatId, response);
            userStates.remove(chatId); // Clear state because we finished the task!

        } catch (BusinessException e) {
            // The service throws this if the code doesn't exist
            sendMessage(chatId, "❌ " + e.getMessage() + "\n\nPlease check your receipt and try typing the code again, or type /cancel.");
        } catch (Exception e) {
            sendMessage(chatId, "❌ An unexpected error occurred. Please try again later.");
        }
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode("Markdown"); // Allows us to use *bold* and `code` formatting!

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message to " + chatId + ": " + e.getMessage());
        }
    }
}