package org.psi.hris.salarypayment;

import java.math.BigDecimal;

import org.psi.hris.PayComponent;

public interface SalaryFormula {
	
	BigDecimal run(PayComponent payComponent);

}
