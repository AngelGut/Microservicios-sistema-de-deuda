package com.debtmanager.webui.service;

import com.debtmanager.webui.client.DebtClient;
import com.debtmanager.webui.dto.request.DebtRequest;
import com.debtmanager.webui.dto.response.DebtResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtService {

    private final DebtClient debtClient;

    public DebtService(DebtClient debtClient) {
        this.debtClient = debtClient;
    }

    public List<DebtResponse> getAll(String token) {
        return debtClient.getAll(token);
    }

    public List<DebtResponse> getByDebtorId(String debtorId, String token) {
        return debtClient.getByDebtorId(debtorId, token);
    }

    public DebtResponse getById(String id, String token) {
        return debtClient.getById(id, token);
    }

    public void create(DebtRequest request, String token) {
        debtClient.create(request, token);
    }
}
