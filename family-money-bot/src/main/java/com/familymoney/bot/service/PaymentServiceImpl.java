package com.familymoney.bot.service;

import com.familymoney.bot.client.AccountClient;
import com.familymoney.bot.client.PaymentCategoryClient;
import com.familymoney.bot.client.PaymentClient;
import com.familymoney.bot.client.UserClient;
import com.familymoney.model.Account;
import com.familymoney.model.BotUser;
import com.familymoney.model.Payment;
import com.familymoney.model.PaymentCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private PaymentClient paymentClient;
    private UserClient userClient;

    public PaymentServiceImpl(PaymentClient paymentClient, UserClient userClient) {
        this.paymentClient = paymentClient;
        this.userClient = userClient;
    }

    @Override
    public Flux<Payment> getAllByTelegramUserId(Integer telegramId) {
        Objects.requireNonNull(telegramId, "telegramId is null.");
        return userClient.getByTelegramId(telegramId)
                .map(BotUser::getId)
                .flatMapMany(paymentClient::findAll);
    }

    @Override
    public Mono<Payment> create(Payment payment) {
        return userClient.resolveUser(payment.getUser()).flatMap(user -> {
            payment.setDate(LocalDate.now());
            payment.setUser(user);
            return paymentClient.create(payment);
        });
    }
}
