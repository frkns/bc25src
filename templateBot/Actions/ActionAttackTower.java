package templateBot.Actions;

import templateBot.Robot;

public class ActionAttackTower extends Action {
    public ActionAttackTower(Robot r) {
        super(r);
    }

    @Override
    public int getScore() {
        return 1;
    }

    @Override
    public void play() {

    }
}
