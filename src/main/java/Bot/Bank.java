package Bot;

import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class Bank {

    private final static Logger log = LoggerFactory.getLogger(Bank.class);

    public static void daily(User user) {
        if ( LocalDate.now().compareTo(Var.getDailyClaimed(user)) > 0) {
            log.info(user.getName() + " has logged in for the day.");
            Var.updateDailyClaimed(user);
        }
    }

}
