package co.tecno.sersoluciones.analityco.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * Created by Ser Soluciones SAS on 03/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Fragment mPrimaryFragment;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

//    public void setItem(Fragment fragment, int position){
//        mFragmentList.set(position, fragment);
//    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add("");
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        //log("position: " + position);
        mPrimaryFragment = (Fragment) object;
    }

    /**
     * Returns currently visible (primary) fragment
     */
    public Fragment getPrimaryFragment() {
        return mPrimaryFragment;
    }

}