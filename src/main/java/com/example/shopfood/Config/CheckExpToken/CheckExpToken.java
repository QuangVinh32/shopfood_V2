package com.example.shopfood.Config.CheckExpToken;
import com.example.shopfood.Model.Entity.Token;
import com.example.shopfood.Repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@EnableAsync
@Component
public class CheckExpToken {
    private static final Logger log = LoggerFactory.getLogger(CheckExpToken.class);
    private static final long EXPIRATION = 864000000L;
    @Autowired
    private TokenRepository tokenRepository;

    @Scheduled(
            cron = "0 0/1 * * * *"
    )
    public void checkExpTokenJob() {
        log.info("time to run log:{}", new Date());
        List<Token> tokensExp = this.tokenRepository.findAllByExpirationIsAfter(new Date(System.currentTimeMillis() + 864000000L));
        this.tokenRepository.deleteAll(tokensExp);
        log.info("number token to delete : {}", tokensExp.size());
    }
}
