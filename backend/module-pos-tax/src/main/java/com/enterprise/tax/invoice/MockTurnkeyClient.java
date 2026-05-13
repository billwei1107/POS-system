/**
 * @file MockTurnkeyClient.java
 * @description 財政部 Turnkey Mock 實作 / Mock TurnkeyClient for Phase 1
 * @description_en Simulates MoF Turnkey responses; always succeeds; replaced by real impl in Phase 3
 * @description_zh 模擬財政部 Turnkey 上傳回應，Phase 1 永遠成功，Phase 3 替換真實實作
 */
package com.enterprise.tax.invoice;

import com.enterprise.tax.entity.Invoice;
import com.enterprise.tax.entity.InvoiceAllowance;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockTurnkeyClient implements TurnkeyClient {

    @Override
    public TurnkeyResult uploadInvoice(Invoice invoice) {
        String ref = "TK-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String resp = "{\"status\":\"SUCCESS\",\"invoiceNo\":\"" + invoice.getFullInvoiceNo() + "\",\"sandbox\":true}";
        return TurnkeyResult.ok(ref, resp);
    }

    @Override
    public TurnkeyResult voidInvoice(Invoice invoice) {
        String ref = "TK-VOID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String resp = "{\"status\":\"VOIDED\",\"invoiceNo\":\"" + invoice.getFullInvoiceNo() + "\",\"sandbox\":true}";
        return TurnkeyResult.ok(ref, resp);
    }

    @Override
    public TurnkeyResult uploadAllowance(InvoiceAllowance allowance) {
        String ref = "TK-ALW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String resp = "{\"status\":\"SUCCESS\",\"allowanceNo\":\"" + ref + "\",\"sandbox\":true}";
        return TurnkeyResult.ok(ref, resp);
    }
}
