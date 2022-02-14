package org.psi.hris.salarypayment;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.psi.hris.SalaryPayment;

public class EmployeeValidatorImpl implements EmployeeValidator {

  @Override
  public List<BusinessPartner> get(SalaryPayment salaryPayment) {
    
    Date startGaji = salaryPayment.getStartingDate();
    Date endGaji = salaryPayment.getEndingDate();
    
    OBCriteria<BusinessPartner> query = OBDal.getInstance().createCriteria(BusinessPartner.class);
    query.add(Restrictions.eq(BusinessPartner.PROPERTY_EMPLOYEE, true));
    query.add(Restrictions.le(BusinessPartner.PROPERTY_HRJOINDATE, startGaji));
    query.add(Restrictions.ge(BusinessPartner.PROPERTY_HRRETIREDATE, endGaji));
    
    return query.list();
  }

}
