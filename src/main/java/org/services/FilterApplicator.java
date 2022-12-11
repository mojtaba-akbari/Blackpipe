package org.services;

import org.ServiceModel.ElementHolder;
import org.baseFilters.BaseFilterModel;
import org.baseFilters.BaseFilterModelAnnotation;

public class FilterApplicator<T extends BaseFilterModel> {
    T filterType;

    public FilterApplicator(T filterType) {
        this.filterType = filterType;
    }

    public ElementHolder executeInitial(ElementHolder element) {
        // Check Sample Processing From Base Model Filter //
        int i = 0;
        if (Boolean.valueOf(this.filterType.getClass().getAnnotation(BaseFilterModelAnnotation.class).IsNeededSample()))
            i = 1; // Go Next Filter

        for (i = 0; i < filterType.getInitialFilterList().size(); i++) {
            element = filterType.getInitialFilterList().get(i).apply(element);
        }

        return element;
    }

    public ElementHolder execute(ElementHolder record) {
        String tmpRecord = record.message; // Create New Pointer On Heap //
        for (int i = 0; i < filterType.getRecordFilterList().size(); i++) {
            record = filterType.getRecordFilterList().get(i).apply(record);
        }
        return record;
    }

    public ElementHolder executeEnd(ElementHolder element) {
        for (int i = 0; i < filterType.getEndFilterList().size(); i++) {
            element = filterType.getEndFilterList().get(i).apply(element);
        }

        return element;
    }
}
