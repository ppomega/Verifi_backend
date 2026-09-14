package com.bgv.platform.dto;

import com.bgv.platform.model.enums.VerificationStatus;
import com.bgv.platform.model.enums.VerificationType;

public class VerificationRequest {

    private VerificationType type;
    private VerificationStatus status;
    private String remarks;
    private String verifiedBy;

    public VerificationType getType() {
        return type;
    }

    public void setType(VerificationType type) {
        this.type = type;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
}
