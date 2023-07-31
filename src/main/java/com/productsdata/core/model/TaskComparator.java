package com.productsdata.core.model;

import java.util.Comparator;

import com.productsdata.core.entity.Tasks;

public class TaskComparator implements Comparator<Tasks> {

    @Override
    public int compare(Tasks task1, Tasks task2) {
        int dateComparison = task1.getDueDate().compareTo(task2.getDueDate());
        if (dateComparison != 0) {
            return dateComparison;
        }

        return task1.getPriority().compareTo(task2.getPriority());
    }
}
