/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

//Simple ViewPagerAdapter setup
//Used to enable swiping with BottomNavigationView
public class VPAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragList = new ArrayList<>();

    public VPAdapter(FragmentManager manager) {
        super(manager);
    }
    @Override
    public Fragment getItem(int pos) {
        return fragList.get(pos);
    }

    @Override
    public int getCount() {
        return fragList.size();
    }

    public void addFrag(Fragment frag) {
        fragList.add(frag);
    }
}