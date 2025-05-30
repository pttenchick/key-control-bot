package app.session;

import lombok.Data;

@Data
public class MySession {

    private Long chatId;
    private int step;

    public MySession(){
        this.step = 0;
    }

    public void nextStep() {
        step++;
    }
}
