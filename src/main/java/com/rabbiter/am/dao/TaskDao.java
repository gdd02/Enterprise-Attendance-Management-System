package com.rabbiter.am.dao;

import com.rabbiter.am.entity.Apply;
import com.rabbiter.am.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskDao {
    int deleteById(String id);

    int insert(Task record);

    Task selectById(String id);

    int update(Task record);

    List<Task> getAllTodo(@Param("receiveNumber")String receiveNumber);

    List<Task> getAllHistoric(@Param("receiveNumber")String receiveNumber);

    Apply findByApplyID(Task task);

    List<Task> getAll(@Param("receiveNumber")String receiveNumber);
}