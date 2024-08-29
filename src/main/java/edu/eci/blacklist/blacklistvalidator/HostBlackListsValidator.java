/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.blacklist.blacklistvalidator;

import edu.eci.blacklist.threads.SearchThread;
import edu.eci.blacklist.spamkeywordsdatasource.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    private List<SearchThread> searchThreads;

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int threads){
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        searchThreads = new ArrayList<>();

        int totalLists = skds.getRegisteredServersCount();
        int baseSize = totalLists / threads;
        int remainder = totalLists % threads;
        int startIndex = 0;

        for(int i = 0; i < threads; i++) {
            int segmentSize = (i < remainder) ? baseSize + 1 : baseSize;
            int endIndex = startIndex + segmentSize - 1;

            SearchThread thread = new SearchThread(startIndex, endIndex, ipaddress, blackListOcurrences);
            searchThreads.add(thread);
            thread.start();

            startIndex = endIndex + 1;
        }
        // Esperar a que todos los hilos terminen su ejecución
        for(SearchThread thread : searchThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Thread interrupted", e);
            }
        }
        int checkedListsCount = 0;

        for(SearchThread thread : searchThreads){
            checkedListsCount += thread.getCheckedListsCount();
        }
        if (blackListOcurrences.size() >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        return blackListOcurrences;
    }
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
}
