package com.rabbiter.am.service;

import com.rabbiter.am.dao.CheckDao;
import com.rabbiter.am.entity.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CheckService {

    @Autowired
    private CheckDao checkDao;

    @Value("${attendance.on-time}")
    private String onTime;

    @Value("${attendance.off-time}")
    private String offTime;


    public int deleteById(String id) {
        return checkDao.deleteById(id);
    }

    /**
     * 根据ID查询打卡记录
     */
    public Check selectById(String id) {
        return checkDao.selectById(id);
    }

    public int insert(Check check) {
        return checkDao.insert(check);
    }

    public int checkOn(Check check) throws ParseException {

        if(ObjectUtils.isEmpty(check.getEmployeeID())) {
            return 0;
        }
        check.setId(UUID.randomUUID().toString());
        SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 =new SimpleDateFormat("HH:mm:ss");
        String date1 = sdf1.format(check.getCheckOnTime());
        check.setDate(date1);
        String date2 = sdf2.format(check.getCheckOnTime());
        Date time = sdf2.parse(date2);
        String time1 = onTime + ":00";
        Date time2;
        try {
            time2 = sdf2.parse(time1);
        } catch (ParseException e) {
            e.printStackTrace();
            time2 = sdf2.parse("08:30:00");
        }
        if(time.before(time2)){
            check.setCheckOnStatus("正常");
        }else {
            check.setCheckOnStatus("迟到");
        }
        checkDao.insert(check);
        return 0;
    }

    /**
     * 上班打卡并返回打卡状态
     * @param check 打卡信息
     * @return 包含打卡状态的Check对象
     * @throws ParseException 日期解析异常
     */
    public Check checkOnWithStatus(Check check) throws ParseException {
        if(ObjectUtils.isEmpty(check.getEmployeeID())) {
            return null;
        }
        check.setId(UUID.randomUUID().toString());
        SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 =new SimpleDateFormat("HH:mm:ss");
        String date1 = sdf1.format(check.getCheckOnTime());
        check.setDate(date1);
        String date2 = sdf2.format(check.getCheckOnTime());
        Date time = sdf2.parse(date2);
        String time1 = onTime + ":00";
        Date time2;
        try {
            time2 = sdf2.parse(time1);
        } catch (ParseException e) {
            e.printStackTrace();
            time2 = sdf2.parse("08:30:00");
        }
        if(time.before(time2)){
            check.setCheckOnStatus("正常");
        }else {
            check.setCheckOnStatus("迟到");
        }
        checkDao.insert(check);
        return check;
    }

    public int checkOff(Check check) throws ParseException {
        if(ObjectUtils.isEmpty(check.getEmployeeID())) {
            return 0;
        }
        SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 =new SimpleDateFormat("HH:mm:ss");
        String date1 = sdf1.format(check.getCheckOffTime());
        Check check1 = new Check();
        check1.setEmployeeID(check.getEmployeeID());
        check1.setDate(date1);
        Check check2 = findByNumberAndDate(check1);
        String date2 = sdf2.format(check.getCheckOffTime());
        Date time = sdf2.parse(date2);
        String time1 = offTime + ":00";
        Date time2 = null;
        try {
            time2 = sdf2.parse(time1);
        } catch (ParseException e) {
            time2 = sdf2.parse("17:30:00");
        }
        if(time.after(time2)){
            check2.setCheckOffStatus("正常");
        }else {
            check2.setCheckOffStatus("早退");
        }
        check2.setCheckOffTime(check.getCheckOffTime());
        checkDao.update(check2);
        return 0;
    }

    /**
     * 下班打卡并返回打卡状态
     * @param check 打卡信息
     * @return 包含打卡状态的Check对象
     * @throws ParseException 日期解析异常
     */
    public Check checkOffWithStatus(Check check) throws ParseException {
        if(ObjectUtils.isEmpty(check.getEmployeeID())) {
            return null;
        }
        SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 =new SimpleDateFormat("HH:mm:ss");
        String date1 = sdf1.format(check.getCheckOffTime());
        Check check1 = new Check();
        check1.setEmployeeID(check.getEmployeeID());
        check1.setDate(date1);
        Check check2 = findByNumberAndDate(check1);
        String date2 = sdf2.format(check.getCheckOffTime());
        Date time = sdf2.parse(date2);
        String time1 = offTime + ":00";
        Date time2 = null;
        try {
            time2 = sdf2.parse(time1);
        } catch (ParseException e) {
            time2 = sdf2.parse("17:30:00");
        }
        if(time.after(time2)){
            check2.setCheckOffStatus("正常");
        }else {
            check2.setCheckOffStatus("早退");
        }
        check2.setCheckOffTime(check.getCheckOffTime());
        checkDao.update(check2);
        return check2;
    }

    public int getCheckOn(Check check){
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(check.getCheckOnTime());
        check.setDate(date);
        Check check1 = findByNumberAndDate(check);
        if(check1 != null){
            if(check1.getCheckOnTime() != null){
                return 1;
            }else if(check1.getRemarks() != null){
                return 2;
            }else {
                return 0;
            }
        }else {
            return 0;
        }
    }

    public int getCheckOff(Check check){
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(check.getCheckOffTime());
        check.setDate(date);
        Check check1 = findByNumberAndDate(check);
        if(check1 != null){
            if(check1.getCheckOffTime() != null){
                return 1;
            }else {
                return 0;
            }
        }else {
            return 0;
        }
    }

    public int update(Check check) {
        return checkDao.update(check);
    }

    public List<Check> findByNumber(String number) {
        return checkDao.findByNumber(number);
    }

    public List<Check> findByMonth(String month) {
        return checkDao.findByMonth(month);
    }

    public List<Check> findByNumberAndMonth(Check check) {
        return checkDao.findByNumberAndMonth(check);
    }

    public Check findByNumberAndDate(Check check) {
        return checkDao.findByNumberAndDate(check);
    }

    public int getWorkDay(String date) {
        String y = date.substring(0,4);
        String m = date.substring(5,7);
        int year = Integer.parseInt(y);
        int month = Integer.parseInt(m);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        // 月份是从0开始计算，所以需要减1
        c.set(Calendar.MONTH, month - 1);

        // 当月最后一天的日期
        int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 开始日期为1号
        int start = 1;
        // 计数
        int count = 0;
        while (start <= max) {
            c.set(Calendar.DAY_OF_MONTH, start);
            if (isWorkDay(c)) {
                count++;
            }
            start++;
        }
        return count;
    }

    public boolean isWorkDay(Calendar c) {
        // 获取星期,1~7,其中1代表星期日，2代表星期一 ... 7代表星期六
        int week = c.get(Calendar.DAY_OF_WEEK);
        // 不是周六和周日的都认为是工作日
        return week != Calendar.SUNDAY && week != Calendar.SATURDAY;
    }

    public List<Check> getCheckDay(Check check){
        check.setDate(check.getDate().substring(0,7));
        return checkDao.getCheckDay(check);
    }

    public List<Check> getLateDay(Check check) {
        check.setDate(check.getDate().substring(0,7));
        return checkDao.getLateDay(check);
    }

    public List<Check> getLeaveEarlyDay(Check check) {
        check.setDate(check.getDate().substring(0,7));
        return checkDao.getLeaveEarlyDay(check);
    }

    public int getCheckDayNumber(Check check){
        List<Check> checkList = getCheckDay(check);
        return checkList.size();
    }

    public int getLateDayNumber(Check check){
        List<Check> checkList = getLateDay(check);
        return checkList.size();
    }

    public int getLeaveEarlyDayNumber(Check check){
        List<Check> checkList = getLeaveEarlyDay(check);
        return checkList.size();
    }

    public List<Check> getLeaveDay(Check check){
        return checkDao.getLeaveDay(check);
    }

    public int getLeaveDayNumber(Check check,String leaveType){
        int days = 0;
        List<Check> checkList = getLeaveDay(check);
        for(Check item : checkList){
            if(item.getRemarks() != null){
                if(leaveType.equals(item.getRemarks())){
                    days = item.getDays();
                }
            }
        }
        return days;
    }

    public int getAllLeaveDay(Check check){
        int days = 0;
        List<Check> checkList = getLeaveDay(check);
        for(Check item : checkList){
            if(item.getRemarks() != null){
                days += item.getDays();
            }
        }
        return days;
    }

    /**
     * 获取今天的打卡状态（包含迟到/早退信息）
     * @param check 包含员工ID和日期
     * @return 今天的打卡记录，包含打卡状态
     */
    public Check getTodayCheckStatus(Check check) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(check.getCheckOnTime() != null ? check.getCheckOnTime() : new Date());
        check.setDate(date);
        return findByNumberAndDate(check);
    }

    /**
     * 获取月度考勤状态（用于日历显示）
     * @param employeeID 员工工号
     * @param month 月份（格式：yyyy-MM）
     * @return 状态Map，key为日期（yyyy-MM-dd），value为状态
     *         NORMAL - 正常打卡（绿色）
     *         ABNORMAL - 异常打卡，迟到或早退（红色）
     *         INCOMPLETE - 未完成打卡，只打了上班或下班（红色）
     *         NO_RECORD - 无打卡记录（红色）
     */
    public Map<String, String> getMonthlyCheckStatus(String employeeID, String month) {
        Map<String, String> statusMap = new HashMap<>();
        
        try {
            // 解析月份
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            Date monthDate = monthFormat.parse(month);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(monthDate);
            
            // 获取该月的所有工作日（1号到最后一天）
            int year = calendar.get(Calendar.YEAR);
            int monthNum = calendar.get(Calendar.MONTH);
            calendar.set(year, monthNum, 1);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // 获取当前日期（用于判断是否是今天及以前）
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            // 查询该月的所有打卡记录
            Check queryCheck = new Check();
            queryCheck.setEmployeeID(employeeID);
            queryCheck.setDate(month); // 月份作为查询条件
            List<Check> monthChecks = checkDao.findByNumberAndMonth(queryCheck);
            
            // 将打卡记录转换为 Map，key 为日期字符串
            Map<String, Check> checkMap = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Check check : monthChecks) {
                if (check.getDate() != null) {
                    String dateStr = check.getDate();
                    checkMap.put(dateStr, check);
                }
            }
            
            // 遍历该月的每一天（只到今天为止）
            for (int day = 1; day <= maxDay; day++) {
                calendar.set(year, monthNum, day);
                Date currentDate = calendar.getTime();
                
                // 只处理今天及以前的日期
                if (currentDate.after(today.getTime())) {
                    break;
                }
                
                // 跳过周末（可选，根据需求）
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                // 如果需要跳过周末，取消下面的注释
                // if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                //     continue;
                // }
                
                String dateStr = dateFormat.format(currentDate);
                Check check = checkMap.get(dateStr);
                
                // 判断今天是否是当前日期
                boolean isToday = dateFormat.format(today.getTime()).equals(dateStr);
                
                if (check == null) {
                    // 无打卡记录
                    statusMap.put(dateStr, "NO_RECORD");
                } else {
                    // 有打卡记录，判断状态
                    boolean hasCheckOn = check.getCheckOnTime() != null && !check.getCheckOnTime().toString().isEmpty();
                    boolean hasCheckOff = check.getCheckOffTime() != null && !check.getCheckOffTime().toString().isEmpty();
                    boolean isLate = "迟到".equals(check.getCheckOnStatus());
                    boolean isEarlyLeave = "早退".equals(check.getCheckOffStatus());
                    
                    if (isToday) {
                        // 今天的特殊处理
                        if (!hasCheckOn || !hasCheckOff) {
                            // 今天未完成打卡
                            statusMap.put(dateStr, "INCOMPLETE");
                        } else if (isLate || isEarlyLeave) {
                            // 今天打卡异常
                            statusMap.put(dateStr, "ABNORMAL");
                        } else {
                            // 今天正常打卡
                            statusMap.put(dateStr, "NORMAL");
                        }
                    } else {
                        // 历史日期
                        if (!hasCheckOn || !hasCheckOff) {
                            // 未完成打卡
                            statusMap.put(dateStr, "INCOMPLETE");
                        } else if (isLate || isEarlyLeave) {
                            // 打卡异常
                            statusMap.put(dateStr, "ABNORMAL");
                        } else {
                            // 正常打卡
                            statusMap.put(dateStr, "NORMAL");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return statusMap;
    }

    /**
     * 获取异常打卡记录（用于补卡申请）
     * 返回迟到或早退的打卡记录
     */
    public List<Check> getAbnormalRecords(Check check) {
        // 修复：前端传的是 month 字段，需要赋值给 date 字段用于SQL查询
        if (check.getMonth() != null && !check.getMonth().isEmpty()) {
            check.setDate(check.getMonth());
        }
        
        List<Check> allRecords = checkDao.findByNumberAndMonth(check);
        List<Check> abnormalRecords = new ArrayList<>();
        
        for (Check record : allRecords) {
            boolean isLate = "迟到".equals(record.getCheckOnStatus());
            boolean isEarlyLeave = "早退".equals(record.getCheckOffStatus());
            
            if (isLate || isEarlyLeave) {
                abnormalRecords.add(record);
            }
        }
        
        return abnormalRecords;
    }
}
