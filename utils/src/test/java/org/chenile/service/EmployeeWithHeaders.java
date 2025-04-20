package org.chenile.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmployeeWithHeaders {
    @JsonProperty("EmployeeID")  // Mapping the CSV column to the Java field
    private int employeeID;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("Department")
    private String department;

    @JsonProperty("Salary")
    private double salary;

    @JsonProperty("StartDate")
    private String startDate;

    // Constructors
    public EmployeeWithHeaders() {}

    public EmployeeWithHeaders(int employeeID, String firstName, String lastName,
                               String department, double salary, String startDate) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.salary = salary;
        this.startDate = startDate;
    }

    // Getters and Setters
    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // toString (optional for logging/debugging)
    @Override
    public String toString() {
        return "Employee{" +
                "employeeID=" + employeeID +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", salary=" + salary +
                ", startDate=" + startDate +
                '}';
    }
}
