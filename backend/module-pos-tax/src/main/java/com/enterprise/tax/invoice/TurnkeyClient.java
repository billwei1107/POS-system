/**
 * @file TurnkeyClient.java
 * @description 財政部 Turnkey 上傳介面 / MoF Turnkey upload client interface
 * @description_en Strategy interface for uploading invoices to the Taiwan MoF e-invoice platform; real impl in Phase 3
 * @description_zh 電子發票上傳至財政部平台的策略介面，Phase 3 串接真實 Turnkey API
 */
package com.enterprise.tax.invoice;

import com.enterprise.tax.entity.Invoice;
import com.enterprise.tax.entity.InvoiceAllowance;

public interface TurnkeyClient {

    // ========================================
    // 上傳發票 / Upload invoice to MoF
    // ========================================
    TurnkeyResult uploadInvoice(Invoice invoice);

    // ========================================
    // 作廢發票 / Void invoice on MoF
    // ========================================
    TurnkeyResult voidInvoice(Invoice invoice);

    // ========================================
    // 上傳折讓單 / Upload allowance to MoF
    // ========================================
    TurnkeyResult uploadAllowance(InvoiceAllowance allowance);

    record TurnkeyResult(boolean success, String referenceNo, String rawResponse, String errorCode, String errorMessage) {
        public static TurnkeyResult ok(String referenceNo, String rawResponse) {
            return new TurnkeyResult(true, referenceNo, rawResponse, null, null);
        }
        public static TurnkeyResult fail(String errorCode, String errorMessage) {
            return new TurnkeyResult(false, null, null, errorCode, errorMessage);
        }
    }
}
