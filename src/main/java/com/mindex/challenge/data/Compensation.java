package com.mindex.challenge.data;

import java.util.Date;

public class Compensation {

    private Employee employee;
    private Date effectiveDate;

    public Compensation(Employee employee, Date effectiveDate) {
        this.employee = employee;
        this.effectiveDate = effectiveDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
