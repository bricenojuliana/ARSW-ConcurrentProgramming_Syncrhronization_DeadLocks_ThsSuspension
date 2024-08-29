package edu.eci.blacklist.threads;

import edu.eci.blacklist.spamkeywordsdatasource.*;

import java.util.LinkedList;
import java.util.List;

public class SearchThread extends Thread {
    private final int lowerLimit;
    private final int upperLimit;
    private final String ipaddress;
    private final List<Integer> blackListOcurrences;
    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private int checkedListsCount;


    public SearchThread(int lowerLimit, int upperLimit, String ipaddress, LinkedList<Integer> blackListOcurrences) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.ipaddress = ipaddress;
        this.blackListOcurrences = blackListOcurrences;
    }

    public void run() {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        checkedListsCount = 0;

        for (int i = lowerLimit; i <= upperLimit && blackListOcurrences.size() < BLACK_LIST_ALARM_COUNT; i++) {
            checkedListsCount++;
            if (skds.isInBlackListServer(i, ipaddress)) {
                synchronized (blackListOcurrences) {
                    if (blackListOcurrences.size() < BLACK_LIST_ALARM_COUNT){
                        blackListOcurrences.add(i);
                    }
                }

            }
        }
    }



    public int getCheckedListsCount() {
        return checkedListsCount;
    }
}
