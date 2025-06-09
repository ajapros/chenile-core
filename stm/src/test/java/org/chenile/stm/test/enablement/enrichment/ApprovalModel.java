package org.chenile.stm.test.enablement.enrichment;

import org.chenile.stm.test.enablement.MfgModel;

public class ApprovalModel extends MfgModel {
    public ApprovalModel(boolean approvalRequired){
        this.approvalRequired = approvalRequired;
    }
    public ApprovalModel() {}
    public boolean approvalRequired = true;
}
