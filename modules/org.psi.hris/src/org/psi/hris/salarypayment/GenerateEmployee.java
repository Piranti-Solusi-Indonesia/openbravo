package org.psi.hris.salarypayment;

import java.util.List;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.psi.hris.PayComponent;
import org.psi.hris.PaySlip;
import org.psi.hris.SalaryComponent;
import org.psi.hris.SalaryPayment;

public class GenerateEmployee extends DalBaseProcess {

	@Override
	protected void doExecute(ProcessBundle bundle) throws Exception {

		final String salaryPaymentId = (String) bundle.getParams().get("HR_Salarypayment_ID");
		var salaryPayment = OBDal.getInstance().get(SalaryPayment.class, salaryPaymentId);
		String employeeValidationClassName = salaryPayment.getEmployeevalidation().getJavaClass();

		Class<?> clazz = Class.forName(employeeValidationClassName);
		Object obj = clazz.getDeclaredConstructor().newInstance();

		EmployeeValidator validator = (obj instanceof EmployeeValidator ? (EmployeeValidator) obj : null);
		if (validator == null)
			throw new OBException(employeeValidationClassName + " is not valid EmployeeValidator type.");

		List<BusinessPartner> employees = validator.get(salaryPayment);

		for (BusinessPartner employee : employees) {

			PaySlip paySlip = OBProvider.getInstance().get(PaySlip.class);
			paySlip.setSalarypayment(salaryPayment);
			paySlip.setEmployee(employee);

			OBDal.getInstance().save(paySlip);

			generatePayComponent(paySlip);

		}

		var result = new OBError();
		result.setType("Success");
		result.setTitle("Success");
		result.setMessage("Employee added: " + employees.size());

		bundle.setResult(result);

	}

	private void generatePayComponent(PaySlip paySlip) {
		OBCriteria<SalaryComponent> seach = OBDal.getInstance().createCriteria(SalaryComponent.class);
		for (SalaryComponent salaryComponent : seach.list()) {
			PayComponent payComponent = OBProvider.getInstance().get(PayComponent.class);
			payComponent.setPayslip(paySlip);
			payComponent.setSalarycomp(salaryComponent);
			payComponent.setAmount(salaryComponent.getConstantamount());

			OBDal.getInstance().save(payComponent);
		}

	}

}
