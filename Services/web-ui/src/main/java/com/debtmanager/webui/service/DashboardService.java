package com.debtmanager.webui.service;

import com.debtmanager.webui.client.*;
import com.debtmanager.webui.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Agrega los datos necesarios para el dashboard.
 *
 * ── ARQUITECTURA ─────────────────────────────────────────────────────────
 * web-ui NO itera servicios ni calcula totales propios.
 * Cada microservicio expone su propio endpoint de resumen/actividad.
 * web-ui hace exactamente UNA llamada por servicio:
 *
 * 1. DebtorClient.getAll() → conteo de deudores registrados
 * 2. DebtClient.getSummary() → KPIs de deudas (totales, montos)
 * 3. PaymentClient.getRecent(7) → últimos 7 pagos (actividad + gráfico)
 * 4. FxClient.getUsdToDop() → tasa de cambio actual
 * 5. ReminderClient.getUpcoming(30) → próximos vencimientos
 *
 * Total: 5 llamadas HTTP paralelas-lógicas, cada una con fallback individual.
 * Si un servicio falla, solo esa sección del dashboard se degrada.
 * ─────────────────────────────────────────────────────────────────────────
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final DebtorClient debtorClient;
    private final DebtClient debtClient;
    private final PaymentClient paymentClient;
    private final FxClient fxClient;
    private final ReminderClient reminderClient;

    public DashboardService(DebtorClient debtorClient,
            DebtClient debtClient,
            PaymentClient paymentClient,
            FxClient fxClient,
            ReminderClient reminderClient) {
        this.debtorClient = debtorClient;
        this.debtClient = debtClient;
        this.paymentClient = paymentClient;
        this.fxClient = fxClient;
        this.reminderClient = reminderClient;
    }

    public Map<String, Object> getDashboardData(String token) {
        Map<String, Object> model = new LinkedHashMap<>();

        // ── 1. Deudores ───────────────────────────────────────────────────────
        List<DebtorResponse> debtors = Collections.emptyList();
        try {
            debtors = debtorClient.getAll(token);
        } catch (Exception e) {
            log.warn("[Dashboard] debtor-service no disponible: {}", e.getMessage());
            model.put("debtorServiceDown", true);
        }
        model.put("totalDebtors", debtors.size());
        model.put("recentDebtors", debtors.stream().limit(10).toList());

        // ── 2. Resumen de deudas por moneda ────────────────────────────────────
        DebtSummaryResponse summaryDOP = debtClient.getSummaryByDOP(token);
        DebtSummaryResponse summaryUSD = debtClient.getSummaryByUSD(token);

        // DOP
        model.put("activeDebts", summaryDOP.totalActivas());
        model.put("paidDebts", summaryDOP.totalPagadas());
        model.put("totalActiveAmount", summaryDOP.montoTotalActivo());
        model.put("totalCollected", summaryDOP.montoTotalCobrado());

        // USD
        model.put("activeDebtsUSD", summaryUSD.totalActivas());
        model.put("paidDebtsUSD", summaryUSD.totalPagadas());
        model.put("totalActiveAmountUSD", summaryUSD.montoTotalActivo());
        model.put("totalCollectedUSD", summaryUSD.montoTotalCobrado());

        // Para el donut chart (DOP)
        long totalDeudas = summaryDOP.totalActivas() + summaryDOP.totalPagadas();
        int cartPct = totalDeudas == 0 ? 0
                : (int) Math.round((summaryDOP.totalActivas() * 100.0) / totalDeudas);
        model.put("cartActivas", summaryDOP.totalActivas());
        model.put("cartPagadas", summaryDOP.totalPagadas());
        model.put("cartTotal", debtors.size());
        model.put("cartPct", cartPct);

        // ── 3. Pagos recientes (UNA llamada) ──────────────────────────────────
        List<RecentPaymentResponse> recentPayments = paymentClient.getRecent(7, token);
        model.put("recentPayments", recentPayments);

        // Montos para el gráfico de barras (se serializa como JSON en la vista)
        List<BigDecimal> paymentAmounts = recentPayments.stream()
                .map(RecentPaymentResponse::amount)
                .toList();
        List<String> paymentLabels = new ArrayList<>();
        for (int i = 0; i < recentPayments.size(); i++) {
            paymentLabels.add("P" + (i + 1));
        }
        model.put("paymentAmounts", paymentAmounts);
        model.put("paymentLabels", paymentLabels);

        // ── 4. Tasa FX (fallback = null → "N/D" en la vista) ─────────────────
        BigDecimal fxRate = fxClient.getUsdToDop(token);
        model.put("fxRate", fxRate);
        model.put("fxOnline", fxRate != null);

        // ── 5. Próximos vencimientos / recordatorios ──────────────────────────
        model.put("upcomingDue", reminderClient.getUpcoming(30, token));

        return model;
    }
}
