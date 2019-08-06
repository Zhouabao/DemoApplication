package com.example.demoapplication.model;

import java.util.Comparator;

/**
 * 专用于按首字母排序
 *
 * @author nanchen
 * @fileName WaveSideBarView
 * @packageName com.nanchen.wavesidebarview
 * @date 2016/12/27  16:19
 * @github https://github.com/nanchen2251
 */

public class LetterComparator implements Comparator<ContactBean> {

    @Override
    public int compare(ContactBean contactModel, ContactBean t1) {
        if (contactModel == null || t1 == null) {
            return 0;
        }
        String lhsSortLetters = contactModel.getIndex().substring(0, 1).toUpperCase();
        String rhsSortLetters = t1.getIndex().substring(0, 1).toUpperCase();

        if (lhsSortLetters.equals("#") && !rhsSortLetters.equals("#"))
            return 1;
        else if (!lhsSortLetters.equals("#") && rhsSortLetters.equals("#"))
            return -1;
        else
            return lhsSortLetters.compareTo(rhsSortLetters);
    }

}
