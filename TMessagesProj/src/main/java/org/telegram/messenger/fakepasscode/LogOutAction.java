package org.telegram.messenger.fakepasscode;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LogOutAction extends AccountAction {
    private static final int WAIT_TIME = 10;

    public LogOutAction() {}

    public LogOutAction(int accountNum) {
        this.accountNum = accountNum;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (Exception ignored) {
        }

        MessagesController.getInstance(accountNum).performLogout(1);
    }
}
