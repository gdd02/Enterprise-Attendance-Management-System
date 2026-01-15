package com.rabbiter.am.service;

import com.rabbiter.am.dao.EmployeeDao;
import com.rabbiter.am.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private EmployeeTypeService employeeTypeService;
    @Autowired
    private PositionService positionService;

    public int deleteById(String id) {
        Employee employee = employeeDao.selectById(id);
        employeeDao.deleteById(id);
        syncDepartment(employee);
        return 200;
    }

    private void syncDepartment(Employee employee) {
        if (employee.getDepartmentID() != null) {
            List<Employee> employees = employeeDao.selectByDepartmentId(employee.getDepartmentID());
            Department department = departmentService.selectById(employee.getDepartmentID());
            department.setQuantity(employees.size());
            departmentService.update(department);
        }
        Position position = null;
        if (employee.getPosition() != null) {
            List<Employee> employees = employeeDao.selectByPositionId(employee.getPosition());
            position = positionService.selectById(employee.getPosition());
            position.setQuantity(employees.size());
            positionService.update(position);
        }
        if (position != null) {
            List<Employee> employees = employeeDao.selectByEmployeeType(position.getTypeID());
            EmployeeType employeeType = employeeTypeService.selectById(position.getTypeID());
            employeeType.setQuantity(employees.size());
            employeeTypeService.update(employeeType);
        }
    }

    public int insert(Employee employee) {
        Employee employee1 = findByNumber(employee.getNumber());
        if (employee1 != null) {
            return 1;
        } else {

            Position position = positionService.selectById(employee.getPosition());
            if(position != null) {
                employee.setEmployeeType(position.getTypeID());
            }
            employee.setId(UUID.randomUUID().toString());
            employee.setPassword("123456");
            employee.setWorkStatus("0");
            employeeDao.insert(employee);
            syncDepartment(employee);
            return 0;
        }
    }

    public Employee selectById(String id) {
        return employeeDao.selectById(id);
    }

    public int update(Employee employee) {
        Position position = positionService.selectById(employee.getPosition());
        if(position != null) {
            employee.setEmployeeType(position.getTypeID());
        }
        employeeDao.update(employee);
        syncDepartment(employee);
        return 1;
    }

    public List<Employee> getAll() {
        return employeeDao.getAll();
    }


    public Employee findByNumber(String number) {
        return employeeDao.findByNumber(number);
    }

    public Employee findByPassword(String password) {
        return employeeDao.findByPassword(password);
    }

    public List<Employee> findByNameAndDepartment(Employee employee) {
        return employeeDao.findByNameAndDepartment(employee);
    }

    public Employee getMinister(Employee employee) {
        return employeeDao.getMinister(employee);
    }

    public Employee getBoss(Employee employee) {
        return employeeDao.getBoss(employee);
    }

    public Employee getLeader(Employee employee) {
        if (employee.getType().equals("1")) {
            Employee employee1 = getMinister(employee);
            if (employee1 != null) {
                return employee1;
            } else {
                Employee employee2 = getBoss(employee);
                return employee2;
            }
        } else if (employee.getType().equals("2")) {
            Employee employee2 = getBoss(employee);
            return employee2;
        } else {
            return employee;
        }
    }

    public List<Chart> getEducation() {
        return employeeDao.getEducation();
    }

    public List<Employee> findByPositionID(String positionID) {
        return employeeDao.findByPositionID(positionID);
    }

    public List<Chart> getAge() {
        return employeeDao.getAge();
    }

    public List<Chart> getNew() {
        return employeeDao.getNew();
    }

}
