package org.psi.hris.salarypayment;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.psi.hris.PayComponent;
import org.psi.hris.PaySlip;
import org.psi.hris.SalaryPayment;

public class CalculateSalary extends DalBaseProcess {

	@Override
	protected void doExecute(ProcessBundle bundle) throws Exception {
		//untuk setiap pay component
		String recordId = (String)bundle.getParams().get("HR_Salarypayment_ID");
		SalaryPayment salaryPayment = OBDal.getInstance().get(SalaryPayment.class, recordId);
		
		List<PayComponent> payComponents = getPayComponentList(salaryPayment);
		
		for (PayComponent payComponent : payComponents) {
			BigDecimal amount = calculateSalaryComponent(payComponent);
			payComponent.setAmount(amount);
			
			OBDal.getInstance().save(salaryPayment);
		}
	}

	private BigDecimal calculateSalaryComponent(PayComponent payComponent) {
		String calculationType = payComponent.getSalarycomp().getCalculationtype();
		
		switch (calculationType) {
		case "CONSTANT":
			return payComponent.getSalarycomp().getConstantamount();
			
		case "JAVA":
			return runJavaSalaryFormula(payComponent);

		default:
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal runJavaSalaryFormula(PayComponent payComponent) {
		String javaclass = payComponent.getSalarycomp().getJavaClass();

		
		try {
			Class<?> clazz = Class.forName(javaclass);
			Object obj = clazz.getDeclaredConstructor().newInstance();

			SalaryFormula formula = (obj instanceof SalaryFormula ? (SalaryFormula) obj : null);
			if (formula == null)
				throw new OBException(javaclass + " is not valid SalaryFormula type.");
			
			return formula.run(payComponent);
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return BigDecimal.ZERO;
	}

	private List<PayComponent> getPayComponentList(SalaryPayment salaryPayment) {
		List<PayComponent> result = new ArrayList<PayComponent>();
		
		List<PaySlip> paySlips = getPaySlip(salaryPayment);
		for (PaySlip paySlip : paySlips) {
			OBCriteria<PayComponent> search = OBDal.getInstance().createCriteria(PayComponent.class);
			search.add(Restrictions.eq(PayComponent.PROPERTY_PAYSLIP, paySlip));
			//TODO ordering how to prioritize salary component calculation
			
			result.addAll(search.list());
		}
		
		return result;
	}

	private List<PaySlip> getPaySlip(SalaryPayment salaryPayment) {
		OBCriteria<PaySlip> search = OBDal.getInstance().createCriteria(PaySlip.class);
		search.add(Restrictions.eq(PaySlip.PROPERTY_SALARYPAYMENT, salaryPayment));
		
		return search.list();
	}

}
