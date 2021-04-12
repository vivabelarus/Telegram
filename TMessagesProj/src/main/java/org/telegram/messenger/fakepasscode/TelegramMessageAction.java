package org.telegram.messenger.fakepasscode;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TelegramMessageAction extends AccountAction implements NotificationCenter.NotificationCenterDelegate {
    public static class Entry {
        public Entry() {}

        public Entry(int userId, String text, boolean addGeolocation) {
            this.userId = userId;
            this.text = text;
            this.addGeolocation = addGeolocation;
        }

        public int userId;
        public String text;
        public boolean addGeolocation;
    }

    public List<Entry> chatsToSendingMessages = new ArrayList<>();

    @JsonIgnore
    private Set<Integer> oldMessageIds = new HashSet<>();

    @Override
    public void execute() {
        if (chatsToSendingMessages.isEmpty()) {
            return;
        }
        NotificationCenter.getInstance(accountNum).addObserver(this, NotificationCenter.messageReceivedByServer);

        SendMessagesHelper messageSender = SendMessagesHelper.getInstance(accountNum);
        MessagesController controller = AccountInstance.getInstance(accountNum).getMessagesController();
        String geolocation = Utils.getLastLocationString();
        for (Entry entry : chatsToSendingMessages) {
            String text = entry.text;
            if (entry.addGeolocation) {
                text += geolocation;
            }
            messageSender.sendMessage(text, entry.userId, null, null, null, false,
                        null, null, null, true, 0);
            MessageObject msg = null;
            for (int i = 0; i < controller.dialogMessage.size(); ++i) {
                if (controller.dialogMessage.valueAt(i).messageText != null &&
                        text.contentEquals(controller.dialogMessage.valueAt(i).messageText)) {
                    msg = controller.dialogMessage.valueAt(i);
                    break;
                }
            }

            if (msg != null) {
                oldMessageIds.add(msg.getId());
                deleteMessage(entry.userId, msg.getId());
            }
        }

        SharedConfig.saveConfig();
    }

    private void deleteMessage(int chatId, int messageId) {
        MessagesController controller = AccountInstance.getInstance(accountNum).getMessagesController();
        ArrayList<Integer> messages = new ArrayList<>();
        messages.add(messageId);
        int channelId = chatId > 0 ? 0 : -chatId;
        controller.deleteMessages(messages, null, null, chatId, channelId, false, false);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        int oldId = (int)args[0];
        TLRPC.Message message = (TLRPC.Message) args[2];
        if (message == null || !oldMessageIds.contains(oldId)) {
            return;
        }
        deleteMessage(Long.valueOf(message.dialog_id).intValue(), message.id);
    }
}
