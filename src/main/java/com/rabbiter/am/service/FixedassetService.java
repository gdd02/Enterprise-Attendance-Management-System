package com.rabbiter.am.service;

import com.rabbiter.am.dao.FixedassetsDao;
import com.rabbiter.am.entity.Employee;
import com.rabbiter.am.entity.Fixedassets;
import com.rabbiter.am.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FixedassetService {

    @Autowired
    FixedassetsDao fixedassetsDao;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskTypeService taskTypeService;
    @Autowired
    private EmployeeService employeeService;

    public int deleteById(String id) {
        return fixedassetsDao.deleteById(id);
    }

    public int insert(Fixedassets fixedassets) {
        Fixedassets fixedassets1 = findByNumber(fixedassets.getNumber());
        if(fixedassets1 != null){
            return 1;
        }else {
            String id1 = UUID.randomUUID().toString();
            fixedassets.setId(id1);
            Employee employee2 = employeeService.findByNumber(fixedassets.getEmployeeNumber());
            if(employee2.getType().equals("3")){
                fixedassets.setStatus("1");
                fixedassets.setApprovalID(employee2.getId());
                fixedassetsDao.insert(fixedassets);
            }else {
                String id2 = UUID.randomUUID().toString();
                fixedassets.setApprovalID(id2);
                fixedassets.setStatus("0");
                fixedassetsDao.insert(fixedassets);
                Task task = new Task();
                task.setId(id2);
                task.setApplyID(id1);
                task.setApplyTime(fixedassets.getApplyTime());
                task.setTypeID(fixedassets.getTaskTypeID());
                task.setApplyNumber(fixedassets.getEmployeeNumber());
                String username = fixedassets.getEmployeeName();
                String typeName = taskTypeService.selectById(fixedassets.getTaskTypeID()).getName();
                task.setName(username+"的"+typeName);
                task.setStatus("0");
                Employee employee = employeeService.findByNumber(fixedassets.getEmployeeNumber());
                Employee employee1 = employeeService.getLeader(employee);
                task.setReceiveNumber(employee1.getNumber());
                taskService.insert(task);
            }
            return 0;
        }
    }

    public Fixedassets selectById(String id) {
        return fixedassetsDao.selectById(id);
    }

    public int update(Fixedassets fixedassets) {
        return fixedassetsDao.update(fixedassets);
    }

    public List<Fixedassets> getAll(){
        return fixedassetsDao.getAll();
    }

    public Fixedassets findByNumber(String number){
        return fixedassetsDao.findByNumber(number);
    }

    public List<Fixedassets> findByEmployeeNumber(String number){
        List<Fixedassets> fixedassetsList = fixedassetsDao.findByEmployeeNumber(number);
        for(Fixedassets item : fixedassetsList){
            if(item.getStatus().equals("1") || item.getStatus().equals("2")){
                Task task = taskService.selectById(item.getApprovalID());
                if(task == null) {
                    continue;
                }
                // 获取批准人信息
                Employee employee = employeeService.findByNumber(task.getApprovalNumber());
                if(employee != null) {
                    item.setApprovalName(employee.getName());
                }
                item.setApprovalTime(task.getApprovalTime());
            }
        }
        return fixedassetsList;
    }

    public List<Fixedassets> getRoomList(){
        return fixedassetsDao.getRoomList();
    }
}
