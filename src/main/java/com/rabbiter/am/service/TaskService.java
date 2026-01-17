package com.rabbiter.am.service;

import com.rabbiter.am.dao.CheckDao;
import com.rabbiter.am.dao.FixedassetsDao;
import com.rabbiter.am.dao.LeaveDao;
import com.rabbiter.am.dao.MakeupCardDao;
import com.rabbiter.am.dao.TaskDao;
import com.rabbiter.am.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskTypeService taskTypeService;
    @Autowired
    private LeaveDao leaveDao;
    @Autowired
    private FixedassetsDao fixedassetsDao;
    @Autowired
    private CheckService checkService;
    @Autowired
    private LeaveTypeService leaveTypeService;
    @Autowired
    private FixedassetTypeService fixedassetTypeService;
    @Autowired
    private MakeupCardDao makeupCardDao;
    @Autowired
    private CheckDao checkDao;

    public int deleteById(String id) {
        return taskDao.deleteById(id);
    }

    public int insert(Task task) {
        taskDao.insert(task);
        return 0;
    }

    public Task selectById(String id) {
        return taskDao.selectById(id);
    }

    public int update(Task task) {
        return taskDao.update(task);
    }

    public List<Task> getAllTodo(String receiveNumber) {
        return taskDao.getAllTodo(receiveNumber);
    }

    public List<Task> getAllHistoric(String receiveNumber) {
        return taskDao.getAllHistoric(receiveNumber);
    }

    public Apply findByApplyID(Task task){
        //task.setStatus(task.getStatus());
        task.setType(taskTypeService.selectById(task.getTypeID()).getName());
        return taskDao.findByApplyID(task);
    }

    public List<Task> getAll(String receiveNumber) {
        List<Task> taskList = taskDao.getAll(receiveNumber);
        for(Task item : taskList){
            if(item.getStatus().equals("0")){
                item.setStatusName("待审批");
            }else if(item.getStatus().equals("1")){
                item.setStatusName("已通过");
            }else if(item.getStatus().equals("2")){
                item.setStatusName("已驳回");
            }
        }
        return taskList;
    }

    public int approval(Task task) throws ParseException {
        task.setType(taskTypeService.selectById(task.getTypeID()).getName());
        Apply apply = taskDao.findByApplyID(task);
        String type = taskTypeService.selectById(task.getTypeID()).getName();
        if(task.getAdvice().equals("yes")){
            task.setStatus("1");
            taskDao.update(task);
            if(type.equals("请假申请")){
                Leave leave = leaveDao.selectById(apply.getId());
                leave.setStatus("1");
                leaveDao.update(leave);
                //循环从开始日期到结束日期中间的所有日期
                List<Check> checkList = new ArrayList<>();
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
                String date1 = sdf.format(leave.getBeginTime());
                String date2 = sdf.format(leave.getEndTime());
                Date beginDate = sdf.parse(date1);
                Date endDate = sdf.parse(date2);
                List<String> dateList = new ArrayList<>();
                dateList.add(date1);
                Calendar calBegin = Calendar.getInstance();
                calBegin.setTime(beginDate);
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(endDate);
                while (endDate.after(calBegin.getTime())){
                    calBegin.add(Calendar.DAY_OF_MONTH,1);
                    dateList.add(sdf.format(calBegin.getTime()));
                }
                for(String item : dateList){
                    Check check = new Check();
                    check.setDate(item);
                    checkList.add(check);
                }
                String remarks = leaveTypeService.selectById(leave.getTypeID()).getName();
                for(Check item : checkList){
                    item.setId(UUID.randomUUID().toString());
                    item.setEmployeeID(leave.getApplyNumber());
                    item.setRemarks(remarks);
                    checkService.insert(item);
                }
            }else if(type.equals("固定资产购置申请") || type.equals("固定资产报废申请")){
                Fixedassets fixedassets = fixedassetsDao.selectById(apply.getId());
                fixedassets.setStatus("1");
                fixedassetsDao.update(fixedassets);
                FixedassetType fixedassetType = fixedassetTypeService.selectById(fixedassets.getTypeID());
                fixedassetType.setQuantity(fixedassetType.getQuantity()+1);
            }else if(type.equals("补卡申请")){
                // 补卡申请审批通过，更新makeup_card表的状态并恢复打卡记录为正常
                com.rabbiter.am.entity.MakeupCard makeupCard = makeupCardDao.selectById(apply.getId());
                if (makeupCard != null) {
                    makeupCard.setStatus("APPROVED");
                    makeupCard.setApprovalEmployeeNumber(task.getApprovalNumber());
                    makeupCard.setApprovalTime(task.getApprovalTime());
                    makeupCardDao.update(makeupCard);
                    
                    // 恢复打卡记录为正常状态
                    String checkId = makeupCard.getCheckId();
                    if (checkId != null && !checkId.isEmpty()) {
                        com.rabbiter.am.entity.Check checkRecord = checkDao.selectById(checkId);
                        if (checkRecord != null) {
                            String abnormalType = makeupCard.getAbnormalType();
                            
                            // 根据异常类型恢复对应的状态和时间
                            try {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String dateStr = checkRecord.getDate() + " "; // 获取日期部分
                                
                                if ("LATE".equals(abnormalType)) {
                                    // 只有迟到，修改上班打卡时间为08:20，状态改为"正常"
                                    checkRecord.setCheckOnTime(sdf.parse(dateStr + "08:20:00"));
                                    checkRecord.setCheckOnStatus("正常");
                                } else if ("EARLY".equals(abnormalType)) {
                                    // 只有早退，修改下班打卡时间为17:40，状态改为"正常"
                                    checkRecord.setCheckOffTime(sdf.parse(dateStr + "17:40:00"));
                                    checkRecord.setCheckOffStatus("正常");
                                } else if ("BOTH".equals(abnormalType)) {
                                    // 既迟到又早退，修改两个时间和状态
                                    checkRecord.setCheckOnTime(sdf.parse(dateStr + "08:20:00"));
                                    checkRecord.setCheckOnStatus("正常");
                                    checkRecord.setCheckOffTime(sdf.parse(dateStr + "17:40:00"));
                                    checkRecord.setCheckOffStatus("正常");
                                }
                                
                                checkDao.update(checkRecord);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 如果时间解析失败，至少更新状态
                                if ("LATE".equals(abnormalType)) {
                                    checkRecord.setCheckOnStatus("正常");
                                } else if ("EARLY".equals(abnormalType)) {
                                    checkRecord.setCheckOffStatus("正常");
                                } else if ("BOTH".equals(abnormalType)) {
                                    checkRecord.setCheckOnStatus("正常");
                                    checkRecord.setCheckOffStatus("正常");
                                }
                                checkDao.update(checkRecord);
                            }
                        }
                    }
                }
            }
            return 0;
        }else if(task.getAdvice().equals("no")){
            task.setStatus("2");
            taskDao.update(task);
            if(type.equals("请假申请")){
                Leave leave = leaveDao.selectById(apply.getId());
                leave.setStatus("2");
                leaveDao.update(leave);
            }else if(type.equals("固定资产购置申请") || type.equals("固定资产报废申请")){
                Fixedassets fixedassets = fixedassetsDao.selectById(apply.getId());
                fixedassets.setStatus("2");
                fixedassetsDao.update(fixedassets);
            }else if(type.equals("补卡申请")){
                // 补卡申请驳回，更新makeup_card表的状态（不恢复打卡记录）
                com.rabbiter.am.entity.MakeupCard makeupCard = makeupCardDao.selectById(apply.getId());
                if (makeupCard != null) {
                    makeupCard.setStatus("REJECTED");
                    makeupCard.setApprovalEmployeeNumber(task.getApprovalNumber());
                    makeupCard.setApprovalTime(task.getApprovalTime());
                    makeupCardDao.update(makeupCard);
                    // 驳回的情况下，打卡记录保持异常状态不变
                }
            }
            return 0;
        }else {
            return 1;
        }
    }
}
