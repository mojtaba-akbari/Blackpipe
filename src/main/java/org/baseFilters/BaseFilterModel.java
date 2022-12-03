package org.baseFilters;

import org.baseModels.baseDataModel;

import java.util.ArrayList;
import java.util.function.Function;

/*
* Do Not USE Regex On Filters Take More Time And Less Performance
* Again Avoid ReplaceAll and some String function , because profiler show that these function use Regex
 */
public class baseFilterModel {
    // Initial Filter Run First //
    // Record Filter Run Per Record //
    // End Filter Run At The End //

    // Filter Applies On Data Sequential //


    // Initial Filters Just Applies One Time At The Initialize  //
    public ArrayList<Function<String,String>> initialFilterList;

    // Record Filters Applies Per Record //
    public ArrayList<Function<String,String>> recordFilterList;

    // End Filters Just Applies One Time On End Of Tasks //
    public ArrayList<Function<String,String>> endFilterList;

    public baseDataModel modelType;

    public baseFilterModel() {
    }

    public ArrayList<Function<String, String>> getInitialFilterList() {
        return initialFilterList;
    }

    public ArrayList<Function<String, String>> getRecordFilterList() {
        return recordFilterList;
    }

    public ArrayList<Function<String, String>> getEndFilterList() {
        return endFilterList;
    }
}
