package Bot;

import net.dv8tion.jda.api.entities.User;

import java.time.LocalDate;

public class Bank {

    public static void daily(User user) {
        if (!user.isBot()) {

            if ( LocalDate.now().compareTo(Var.getDailyClaimed(user.getId())) > 0) {
                System.out.println("Claiming daily for " + user.getName());
                Var.updateDailyClaimed(user.getId());
            }

        }
    }

}
