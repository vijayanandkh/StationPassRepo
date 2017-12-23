package com.vijay.customapp.spacestationpasses.ISSPassInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.Settings.System.DATE_FORMAT;

/**
 * Helper class for providing content of ISSPass Item
 */
public class ISSPassDetail {

    /**
     * An array of sample (ISSPassItem) items.
     */
    public static final List<ISSPassesItem> ITEMS = new ArrayList<ISSPassesItem>(26);


    /**
     * A ISSPassItem representing a piece of pass information having duration and timestamp
     */
    public static class ISSPassesItem {
        public final long risetime;
        public final String risetimeInfo;
        public final int duration;

        public ISSPassesItem(Long risetime, int duration) {
            this.risetime = risetime;
            this.risetimeInfo = new SimpleDateFormat("MM/dd/YY HH:mm:ss").format(new Date(risetime));
            this.duration = duration;
        }


        @Override
        public String toString() {
            return risetimeInfo + " : " + duration ;
        }
    }
}
