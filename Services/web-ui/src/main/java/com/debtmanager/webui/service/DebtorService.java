package com.debtmanager.webui.service;

import com.debtmanager.webui.client.DebtorClient;
import com.debtmanager.webui.dto.request.DebtorRequest;
import com.debtmanager.webui.dto.response.DebtorResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtorService {

    private final DebtorClient debtorClient;

    public DebtorService(DebtorClient debtorClient) {
        this.debtorClient = debtorClient;
    }

    public List<DebtorResponse> getAll(String token) {
        return debtorClient.getAll(token);
    }

    public DebtorResponse getById(String id, String token) {
        return debtorClient.getById(id, token);
    }

    public void create(DebtorRequest request, String token) {
        debtorClient.create(request, token);
    }
}
