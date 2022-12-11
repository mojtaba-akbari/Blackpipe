package org.baseFilters;

import org.ServiceModel.ElementHolder;
import org.baseModels.BaseDataModel;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;

/*
 * Do Not USE Regex On Filters Take More Time And Less Performance
 * Again Avoid ReplaceAll and some String function , because profiler show that these function use Regex
 */
public abstract class BaseFilterModel {
    // Initial Filter Run First //
    // Record Filter Run Per Record //
    // End Filter Run At The End //

    // Filter Applies On Data by Sequential FilterApplicator executed //

    // Initial Filters Just Applies One Time At The Initialize  //
    public ArrayList<Function<ElementHolder, ElementHolder>> initialFilterList;
    // Record Filters Applies Per Record //
    public ArrayList<Function<ElementHolder, ElementHolder>> recordFilterList;
    // End Filters Just Applies One Time On End Of Tasks //
    public ArrayList<Function<ElementHolder, ElementHolder>> endFilterList;
    public BaseDataModel modelType;
    // Sample //
    StringBuilder sample;

    public BaseFilterModel() {
    }

    public StringBuilder getSample() {
        return sample;
    }

    public void setSample(StringBuilder sample) {
        this.sample = sample;
    }

    public ArrayList<Function<ElementHolder, ElementHolder>> getInitialFilterList() {
        return initialFilterList;
    }

    public ArrayList<Function<ElementHolder, ElementHolder>> getRecordFilterList() {
        return recordFilterList;
    }

    public ArrayList<Function<ElementHolder, ElementHolder>> getEndFilterList() {
        return endFilterList;
    }

    public abstract void takeSample(Scanner scannerFile); // Override in any filter that need sample //
}
