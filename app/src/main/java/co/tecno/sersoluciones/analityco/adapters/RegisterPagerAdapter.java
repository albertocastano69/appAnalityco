package co.tecno.sersoluciones.analityco.adapters;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.tecno.sersoluciones.analityco.fragments.ProfileFragment;
import co.tecno.sersoluciones.analityco.fragments.RegisterStepOneFragment;
import co.tecno.sersoluciones.analityco.fragments.RegisterStepTwoFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * Created by Ser Soluciones SAS on 14/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class RegisterPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Fragment mPrimaryFragment;

    public RegisterPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragmentList.add(new RegisterStepOneFragment());
        mFragmentList.add(new RegisterStepTwoFragment());
        mFragmentList.add(new ProfileFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void setProfileFragment() {
        mFragmentList.set(2, new ProfileFragment());
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryFragment = (Fragment) object;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Todos";
            case 1:
                return "Activos";
        }
        return mFragmentTitleList.get(position);
    }

    /**
     * Returns currently visible (primary) fragment
     */
    public Fragment getPrimaryFragment() {
        return mPrimaryFragment;
    }

}