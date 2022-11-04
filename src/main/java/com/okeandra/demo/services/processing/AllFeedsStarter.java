package com.okeandra.demo.services.processing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AllFeedsStarter implements Processing{

    @Autowired
    FeedInsales feedInsales;

    @Autowired
    SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay;

    @Autowired
    FeedSberDutyFree feedSberDutyFree;

    @Autowired
    FeedGroupPrice feedGroupPrice;

    @Autowired
    FeedSelvis feedSelvis;

    @Override
    public List<String> start() {
        List<Processing> todoList = new ArrayList<>();
        todoList.add(feedInsales);
        todoList.add(sberFeedAll0DayKapousOllinOneDay);
        todoList.add(feedSberDutyFree);
        todoList.add(feedGroupPrice);
        todoList.add(feedSelvis);

        todoList.forEach(Processing::start);
        List<String> result = new ArrayList<>(Arrays.asList("Все фиды созданы успешно"));
        return result;
    }



}
