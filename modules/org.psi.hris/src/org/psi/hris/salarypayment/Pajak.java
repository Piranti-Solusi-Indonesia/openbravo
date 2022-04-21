package org.psi.hris.salarypayment;

import java.math.BigDecimal;

import javax.management.OperationsException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.psi.hris.PayComponent;
import org.psi.hris.PaySlip;
import org.wirabumi.common.ContractException;
import org.wirabumi.payroll.Salary;
import org.wirabumi.payroll.SalaryBuilder;
import org.wirabumi.payroll.SalaryFactory;
import org.wirabumi.tax.TaxCalculationType;
import org.wirabumi.tax.TaxCalculatorFactory;
import org.wirabumi.tax.TaxDimension;

public class Pajak implements SalaryFormula {

	@Override
	public BigDecimal run(PayComponent payComponent) {
		
		PaySlip paySlip = payComponent.getPayslip();
		OBCriteria<PayComponent> search = OBDal.getInstance().createCriteria(PayComponent.class);
		search.add(Restrictions.eq(PayComponent.PROPERTY_PAYSLIP, paySlip));
		
		BigDecimal totalPendapatan = BigDecimal.ZERO;
		for (PayComponent component : search.list()) {
			if (!component.getSalarycomp().isEarning())
				continue;
			
			totalPendapatan = totalPendapatan.add(component.getAmount());
		}
		
		//FIXME replace hard coded parameter with correct one
		TaxDimension taxDimension = null;
		try {
			taxDimension = new TaxDimension(true, 3, "some npwp", false, TaxCalculationType.Standard);
			SalaryFactory sf = new SalaryFactory();
			TaxCalculatorFactory tcf = new TaxCalculatorFactory();
			SalaryBuilder salaryBuilder = new SalaryBuilder(taxDimension, sf, tcf);
			salaryBuilder
				.recurringPay(totalPendapatan)
				.nonRecurringPay(BigDecimal.ZERO); //FIXME replace with bonus
			Salary salary = salaryBuilder.build();
				
			salary.recalculateIncomeTax();
			
			return salary.getIncomeTax();
			
		} catch (ContractException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// return value
		return BigDecimal.ZERO;
		

	}

}
