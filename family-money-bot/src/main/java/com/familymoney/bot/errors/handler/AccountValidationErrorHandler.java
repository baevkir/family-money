package com.familymoney.bot.errors.handler;

import com.familymoney.bot.errors.AccountValidationException;
import com.familymoney.bot.service.AccountService;
import com.sessionbot.errors.handler.ErrorHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountValidationErrorHandler implements ErrorHandler<AccountValidationException> {
    private AccountService accountService;

    public AccountValidationErrorHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Mono<? extends BotApiMethod<?>> handle(AccountValidationException exception) {
        Long chatId = exception.getErrorData().getCommandRequest().getCommandMessage().getChatId();
        Integer telegramId = exception.getErrorData().getCommandRequest().getCommandMessage().getFrom().getId();
        return accountService.getAllForTelegramUserId(telegramId).collect(Collectors.toList())
            .map(paymentTypes -> {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = paymentTypes.stream()
                        .map(paymentType ->
                                new InlineKeyboardButton().setText(paymentType.getName()).setCallbackData(paymentType.getName()))
                        .collect(Collectors.toList());

                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);

                return new SendMessage().setChatId(chatId).setText(exception.getMessage()).setReplyMarkup(markupInline);
            });
    }
}
