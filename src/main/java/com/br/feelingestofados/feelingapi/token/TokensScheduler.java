package com.br.feelingestofados.feelingapi.token;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.List;

@Configuration
@EnableScheduling
@NoArgsConstructor
public class TokensScheduler {
    private final static int executionRate = 1800000; // 30 minutes
    private final static int expirationTime = 28800000; // 8 hours

    @Scheduled(fixedRate = executionRate)
    public static void checkIfAnyTokenShouldBeDeleted() {
        List<Token> validTokens = TokensManager.getInstance().getValidTokens();
        long currentDate = Calendar.getInstance().getTimeInMillis();
        validTokens.forEach(token -> {
            if((currentDate - token.getCreatedAt()) >= expirationTime) {
                token.invalidateToken();
            }
        });
        TokensManager.getInstance().removeInvalidTokens();
    }
}
