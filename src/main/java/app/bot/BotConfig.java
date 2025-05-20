package app.bot;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Setter
@Getter
@Component
public class BotConfig {

        /**
         * Telegram bot username (required)
         * Example: @my_custom_bot
         */
        @Value("${telegram.bot.username}")
        private String name;

        /**
         * Telegram bot token (required)
         * Example: 1234567890:AbCDeEn10XcYV2Key1fBCjOqPtqT9RQYmUQ
         */
        @Value("${telegram.bot.token}")
        private String token;


}
