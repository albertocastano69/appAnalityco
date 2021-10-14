package co.tecno.sersoluciones.analityco.adapters.project;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.tecno.sersoluciones.analityco.steps.ProjectStep1;
import co.tecno.sersoluciones.analityco.steps.ProjectStep2;
import co.tecno.sersoluciones.analityco.steps.ProjectStep3;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * Created by Ser Soluciones SAS on 08/02/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class WizardProjectPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Fragment mPrimaryFragment;

    public WizardProjectPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragmentList.add(new ProjectStep1());
        mFragmentList.add(new ProjectStep3());
        mFragmentList.add(new ProjectStep2());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
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
                return "Datos del Proyecto";
            case 1:
                return "Etapas del proyecto";
            case 2:
                return "Empresas Asociadas";
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