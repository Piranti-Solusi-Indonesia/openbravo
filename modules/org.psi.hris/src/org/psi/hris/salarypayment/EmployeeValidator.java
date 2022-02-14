package org.psi.hris.salarypayment;

import java.util.List;

import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.psi.hris.SalaryPayment;

public interface EmployeeValidator {
  
  List<BusinessPartner> get(SalaryPayment salaryPayment);

}
