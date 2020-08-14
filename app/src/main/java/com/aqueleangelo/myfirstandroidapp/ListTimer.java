package com.aqueleangelo.myfirstandroidapp;

import java.util.ArrayList;

public class ListTimer {
    ArrayList<Integer> timerTimes = new ArrayList<>();
    ArrayList<Integer> activityNumbs = new ArrayList<>();

    public ListTimer(ArrayList<Integer> activityTimes, int acclimationTime){
        int size = activityTimes.size() * 2;
        int activityIndex = 0;


        timerTimes.add(acclimationTime);
        //activityNumbs.add(activityIndex);

        for (int i = 1; i < size; i++){
            int newTime = timerTimes.get(i-1) + activityTimes.get(activityIndex);
            timerTimes.add(newTime);
            activityNumbs.add(activityIndex);

            if(i % 2 == 0)
                activityIndex++;
        }

        activityNumbs.add(activityIndex);
    }

    public ArrayList<Integer> getTimerTimes() { return timerTimes; }
    public ArrayList<Integer> getActivityNumbs() { return activityNumbs; }

    public int checkForEvent(int timeGiven){
        for (int i = 0; i < timerTimes.size(); i++){
            if(timerTimes.get(i) == timeGiven){
                return activityNumbs.get(i);
            }
        }
        return -1;
    }
}
