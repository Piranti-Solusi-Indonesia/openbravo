package org.psi.hris.approval;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.psi.hris.SalaryPayment;

public class Approval extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    
    var result = new OBError();
    result.setType("Success");
    result.setTitle("Success");
    
    String submitApprover = (String) bundle.getParams().get("HR_Salarypayment_ID");
    String approvalAction = (String) bundle.getParams().get("HR_Salarypayment_ID");
    
    String recordId = (String) bundle.getParams().get("HR_Salarypayment_ID");
    SalaryPayment salary = OBDal.getInstance().get(SalaryPayment.class, recordId);
    
    if (approvalAction.equalsIgnoreCase("RECHECK")) {
      
      //TODO notify submit approval + everyone that approve=Y
      
      salary.setDocumentStatus("DR");
      salary.setSubmitappr("SUBMIT");
      OBDal.getInstance().save(salary);
      
      result.setMessage("Need to recheck, status back to DRAFT");
      bundle.setResult(result);
      return;
    }
    
    switch (submitApprover) {
      case "SUBMIT":
        salary.setDocumentStatus("CO");
        salary.setSubmitappr("APPROVER1");
        OBDal.getInstance().save(salary);
        
        //TODO update approval list
        //TODO notify approver1
        break;
        
      case "APPROVER1":
        salary.setSubmitappr("APPROVER2");
        OBDal.getInstance().save(salary);
        
        //TODO update approval list
        //TODO notify approver1
        break;
        
      case "APPROVER2":
        salary.setSubmitappr("APPROVER3");
        OBDal.getInstance().save(salary);
        
        //TODO update approval list
        //TODO notify approver1
        break;
        
      case "APPROVER3":
        //TODO update approval list
        break;

      default:
        throw new OBException("undefined submit approver: "+submitApprover);
    }
    
    result.setMessage("approval updated");
    bundle.setResult(result);
  }

}
