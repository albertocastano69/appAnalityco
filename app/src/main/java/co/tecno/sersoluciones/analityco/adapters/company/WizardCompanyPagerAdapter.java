package co.tecno.sersoluciones.analityco.adapters.company;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.tecno.sersoluciones.analityco.fragments.company.CreateStepOneFragment;
import co.tecno.sersoluciones.analityco.fragments.company.CreateStepThreeFragment;
import co.tecno.sersoluciones.analityco.fragments.company.CreateStepTwoFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * Created by Ser Soluciones SAS on 13/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class WizardCompanyPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Fragment mPrimaryFragment;

    public WizardCompanyPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFragmentList.add(new CreateStepOneFragment());
        mFragmentList.add(new CreateStepTwoFragment());
        mFragmentList.add(new CreateStepThreeFragment());
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
                return "Datos de la Empresa";
            case 1:
                return "Condiciones de Servicio";
            case 2:
                return "Vincular Usuarios";
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