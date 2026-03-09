package com.debtmanager.webui.service;

import com.debtmanager.webui.client.ReminderClient;
import com.debtmanager.webui.dto.response.ReminderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final ReminderClient reminderClient;

    public ReminderService(ReminderClient reminderClient) {
        this.reminderClient = reminderClient;
    }

    public List<ReminderResponse> getUpcoming(int days, String token) {
        return reminderClient.getUpcoming(days, token);
    }

    public void create(String debtId, String description, String dueDate,
            int daysBefore, String token) {
        reminderClient.create(debtId, description, dueDate, daysBefore, token);
    }
}
