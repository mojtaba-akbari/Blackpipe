package org.model;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class FilterModel {
    // List Of lambda that should affect sequential on a resource //
    ArrayList<Function<String,String>> _listOfFilter_;

    public FilterModel() {
        this._listOfFilter_ = new ArrayList<Function<String,String>>();

        // Load Filter //
        loadFilter();
    }

    public void loadFilter(){
        // Do Not USE Regex On Filters Take More Time And Less Performance //
        // Again Avoid ReplaceAll and some String function , because profiler show that these function use Regex //

        Function<String,String> filter1_makeRecordHTMLTable= (record) -> {
            int delimiter=','; // delimiter for this filter is ,
            int unDelimiterChar='"'; // char that should avoid that if we have seen delimiter char -> ,
            String tr_start="<tr>"; // Point To The String Constant Pool
            String tr_end="</tr>"; // Point To The String Constant Pool
            String th_start="<th>"; // Point To The String Constant Pool
            String th_end="</th>"; // Point To The String Constant Pool
            String replaceRecordCells=th_end+th_start; // Point To The String Constant Pool
            // Best Performance For Creating And Working New String Use Builder //
            StringBuilder _SB_=new StringBuilder();

            // Make Cell Table //
            boolean isSeenUnDelimiterChar= false; // Use Atomic Because Of Parallel

            // Prevent Using foreach //
            char[] charArray=record.toCharArray();
            for(int i=0;i<charArray.length;i++){
                if(charArray[i] == unDelimiterChar) {
                    if(isSeenUnDelimiterChar) isSeenUnDelimiterChar=false;
                    else isSeenUnDelimiterChar=true;
                }
                else if(charArray[i] == delimiter) {
                    if(!isSeenUnDelimiterChar) _SB_.append(replaceRecordCells);
                    else _SB_.append(charArray[i]);
                }
                else _SB_.append(charArray[i]);
            }


            // Make A HTML Table Record //
            _SB_.insert(0,tr_start);
            _SB_.append(tr_end);

            charArray=null;

            return _SB_.toString();
        };

        _listOfFilter_.add(filter1_makeRecordHTMLTable);
    }

    public String execute(String record){
        String tmpRecord=record; // Create New Pointer On Heap //

        for (Function<String,String> function:
             _listOfFilter_) {
            tmpRecord=function.apply(tmpRecord); // Apply Filter //
        }

        return tmpRecord;
    }
}
