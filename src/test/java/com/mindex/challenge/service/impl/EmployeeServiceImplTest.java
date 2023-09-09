package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;

    private String reportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/reportStructure/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    // Test where all employees are on same level
    @Test
    public void testNumberOfReportsOnSameLevel() {

        //Employee manager = createEmployeeTree();
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";

        //Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, employeeId).getBody();

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, employeeId).getBody();

        assertNotNull(reportingStructure);
        assertNotNull(reportingStructure.getEmployee());
        assertEquals(reportingStructure.getEmployee().getEmployeeId(), employeeId);
        assertEquals(reportingStructure.getNumberOfReports(), 4);

    }

    // Helper function to create manager and reports under them
    private Employee createEmployeeTree() {

        Employee manager = new Employee();
        manager.setFirstName("John");
        manager.setLastName("Lennon");

        List<Employee> directReports = new ArrayList<>();

        for(int i = 0; i<4; i++) {
            Employee report = new Employee();
            report.setFirstName("John"+i);
            report.setLastName("Doe"+i);
            Employee createdEmployee = restTemplate.postForEntity(employeeUrl, report, Employee.class).getBody();
            directReports.add(createdEmployee);
        }

        manager.setDirectReports(directReports);
        return restTemplate.postForEntity(employeeUrl, manager, Employee.class).getBody();
    }

    // Test where there is at least one level
    @Test
    public void testNumberOfReportsOnDifferentLevel() {
        Employee manager = createEmployeeTreeSubs();

        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, manager.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertNotNull(reportingStructure.getEmployee());
        assertEquals(reportingStructure.getEmployee().getEmployeeId(), manager.getEmployeeId());
        assertEquals(reportingStructure.getNumberOfReports(), 4);

    }

    private Employee createEmployeeTreeSubs() {

        Employee manager = new Employee();
        manager.setFirstName("John");
        manager.setLastName("Lennon");

        List<Employee> directReports = new ArrayList<>();

        for(int i = 0; i<2; i++) {
            Employee report = new Employee();
            report.setFirstName("John"+i);
            report.setLastName("Doe"+i);
            Employee createdEmployee = restTemplate.postForEntity(employeeUrl, report, Employee.class).getBody();
            Employee reportManager = new Employee();
            reportManager.setFirstName("Fred"+i);
            reportManager.setLastName("Flin"+i);
            reportManager.setDirectReports(Collections.singletonList(createdEmployee));
            Employee subManager = restTemplate.postForEntity(employeeUrl, reportManager, Employee.class).getBody();
            directReports.add(subManager);
        }

        manager.setDirectReports(directReports);
        return restTemplate.postForEntity(employeeUrl, manager, Employee.class).getBody();
    }
}
