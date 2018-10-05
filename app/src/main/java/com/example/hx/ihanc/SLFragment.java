package com.example.hx.ihanc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SLFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Context mContext;
    private View footer;
    private View view;
    private ListView saleListView;

    public SLFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SLFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SLFragment newInstance(String param1, String param2) {
        SLFragment fragment = new SLFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_sl, container, false);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext=context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initViews(Context context) {
        //获得footer+header布局文件
        LayoutInflater inflater =LayoutInflater.from(context);
        saleListView=view.findViewById(R.id.saleListView);
        footer = inflater.inflate(R.layout.footer,null);
        footer.findViewById(R.id.ll_footer).setVisibility(GONE); //初始化时设置footer不可见
        saleListView.addFooterView(footer);
    }
}
