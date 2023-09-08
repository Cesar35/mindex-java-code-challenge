package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure fetchReports(String id) {
        LOG.debug("Fetching reports for employee with id [{}]", id);

        // Fetch current employee
        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        //Call recursive helper method to go through each employees direct reports and count them
        int numberOfReports = countInnerReports(employee);

        //Return new data object with employee and number of employees reporting to them
        return new ReportingStructure(employee, numberOfReports);
    }

    // Recursive loop to count all nested employees
    private int countInnerReports(Employee employee) {
        int totals = 0;
        if(employee.getDirectReports() != null ) {
            totals += employee.getDirectReports().size();
            for (Employee e : employee.getDirectReports()) {
                totals += countInnerReports(e);
            }
        }
        return totals;
    }
}
