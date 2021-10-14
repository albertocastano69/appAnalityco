package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.GlideApp;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.Image;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;


public class ContractGeneralInfoFragmnet extends Fragment {

    private static final String ARG_CONTRACTID = "projectId";
    private static final String ARG_ACTIVE = "isactive";
    private static final String ARG_IMAGES = "images";
    private ClaimsBasicUser Claims;
    private String contractId;
    private String fillForm;
    private User user;

    @BindView(R.id.state_icon)
    ImageView stateIcon;
    @BindView(R.id.header_img)
    ImageView headerImg;

    @BindView(R.id.text_review)
    TextView textName;
    @BindView(R.id.btn_edit)
    public ImageView btnEdit;
    @BindView(R.id.label_validity)
    public TextView labelValidity;
    @BindView(R.id.label_validity_context)
    public TextView labelValidity_context;
    @BindView(R.id.text_active)
    public TextView text_active;
    @BindView(R.id.text_name)
    public TextView text_name;
    @BindView(R.id.text_sub_name)
    public TextView text_sub_name;
    @BindView(R.id.card_view_detail)
    public CardView card_view_detail;
    @BindView(R.id.logo)
    public ImageView logo_imag;
    @BindView(R.id.text_validity)
    public TextView text_validity;

    @BindView(R.id.btn_edit_contract)
    public Button btnEdit_contract;
    @BindView(R.id.text_name_contract)
    public TextView text_name_contract;
    @BindView(R.id.text_sub_name_contract)
    public TextView text_sub_name_contract;
    @BindView(R.id.logo_contract)
    public ImageView logo_imag_contract;

    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editContractButton;
    private Contract contract;
    @BindView(R.id.text_contratType)
    TextView contractType;
    @BindView(R.id.text_contratTypeC)
    TextView contractTypeC;
    @BindView(R.id.see_contract)
    Button openContract;
    @BindView(R.id.title)
    LinearLayout infoContract;
    @BindView(R.id.contractTypeC)
    TextView contractTypeCLabel;
    @BindView(R.id.image_contratType)
    ImageView imageContractType;

    public static ContractGeneralInfoFragmnet newInstance(String contractId, int isactive, String images, String transitionName) {

        ContractGeneralInfoFragmnet fragment = new ContractGeneralInfoFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_CONTRACTID, contractId);
        args.putInt(ARG_ACTIVE, isactive);
        args.putString(ARG_IMAGES, images);
        args.putString(Constantes.ITEM_TRANSITION_NAME, transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contract_general_info_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null && getActivity() != null) {
            Claims = ((DetailsContractsActivityTabs) requireActivity()).dataFragment.getClaims();
            contractId = getArguments().getString(ARG_CONTRACTID);
            fillForm = ((DetailsContractsActivityTabs) requireActivity()).dataFragment.getData();
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            int active = getArguments().getInt(ARG_ACTIVE);
            if (active == 1) {
                stateIcon.setVisibility(View.VISIBLE);
            } else if (active == 2) {
                stateIcon.setVisibility(View.VISIBLE);
                stateIcon.setImageResource(R.drawable.state_icon_red);
            }
            String imageTransitionName = getArguments().getString(Constantes.ITEM_TRANSITION_NAME);
            headerImg.setTransitionName(imageTransitionName);
            headerImg.setImageResource(R.drawable.image_not_available);

            fillForm(fillForm);
        }
        editContractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                project.putExtra("process", 1);
                project.putExtra("contract", contractId);
                project.putExtra("json", fillForm);
                startActivityForResult(project, Constantes.UPDATE);
            }
        });
        openContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contract.ContractFile == null) {
                    MetodosPublicos.alertDialog(getActivity(), "No existe contrato escaneado");
                } else {
                    String url = Constantes.URL_IMAGES + contract.ContractFile;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });
        permission();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void fillForm(String json) {

        logW(json);
        contract = new Gson().fromJson(json,
                new TypeToken<Contract>() {
                }.getType());
        if (contract != null) {
            if (contract.FormImageLogo != null) {
                String url = Constantes.URL_IMAGES + contract.FormImageLogo;
                chargeImage(url, headerImg);
            }
            textName.setText(contract.ContractReview);
            //textNit.setText(contract.DescriptionContractType);
            btnEdit.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            labelValidity.setVisibility(View.GONE);
            text_active.setVisibility(View.GONE);
            //contractNum.setText(contract.ContractNumber);
            //ContractorId= mItem.Id;
            text_name.setText(contract.CompanyName);
            if (contract.IsRegister) {
                logE("isRegistre ture");
                infoContract.setVisibility(View.GONE);
                contractTypeCLabel.setText("Tipo de registro");
            }
            text_sub_name.setText("NIT " + contract.CompanyDocumentNumber);
            if (contract.CompanyLogo != null) {
                String url = Constantes.URL_IMAGES + contract.CompanyLogo;

                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            }
            btnEdit.setVisibility(View.GONE);
            btnEdit_contract.setVisibility(View.GONE);
            contractType.setText(contract.DescriptionPersonalType);
            text_name_contract.setText(contract.ContractorName);
            labelValidity_context.setVisibility(View.INVISIBLE);
            text_sub_name_contract.setText("NIT " + contract.ContractorDocumentNumber);
            if (contract.ContractorLogo != null) {
                String url = Constantes.URL_IMAGES + contract.ContractorLogo;

                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag_contract);
            }
            contractTypeC.setText(contract.DescriptionContractType);
        }
        setImageContractTypePersonal();
    }

    private void chargeImage(String url, ImageView label) {
        String[] format = url.split(Pattern.quote("."));
        if (format[format.length - 1].equals("svg")) {

            GlideApp.with(requireActivity())
                    .as(PictureDrawable.class)
                    .apply(
                            new RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.image_not_available)
                    )
                    .fitCenter()
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .load(url)
                    .into(label);

        } else {
            Picasso.get()
                    .load(url)
                    .noFade()
                    .resize(0, 150)
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.image_not_available)
                    .into(label);
        }
    }

    private void setImageContractTypePersonal() {
        String fillImages;
        fillImages = getArguments().getString(ARG_IMAGES);
        ArrayList<Image> imagesContract;
        imagesContract = new Gson().fromJson(fillImages,
                new TypeToken<ArrayList<Image>>() {
                }.getType());
        String[] tagList;
        if (imagesContract != null) {
            for (int i = 0; i < imagesContract.size(); i++) {
                tagList = imagesContract.get(i).Tags;
                if (tagList[0].equals("AA Personal " + contract.DescriptionPersonalType)) {
                    String url = Constantes.URL_IMAGES + imagesContract.get(i).Logo;
                    Picasso.get().load(url)
                            .resize(50, 50)
                            .placeholder(R.drawable.image_not_available)
                            .error(R.drawable.image_not_available)
                            .into(imageContractType);
                }
            }
        }
    }

    private void permission() {
        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("contracts.update") || user.IsSuperUser)
                editContractButton.setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("contracts.update") || user.IsSuperUser)
                editContractButton.setVisibility(View.VISIBLE);
        }
    }
}
