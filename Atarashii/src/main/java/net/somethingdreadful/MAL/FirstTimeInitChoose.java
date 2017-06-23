package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class FirstTimeInitChoose extends Fragment implements RadioGroup.OnCheckedChangeListener {
    private FirstTimeInit activity1;
    private AddAccount activity2;

    public static FirstTimeInitChoose newInstance(FirstTimeInit activity) {
        FirstTimeInitChoose fragment = new FirstTimeInitChoose();
        fragment.activity1 = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.init_fragment_choose, container, false);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.init_radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        switch(i) {
            case R.id.radio_mal:
                if (activity1 != null) {
                    activity1.setMAL(true);
                    activity1.getFirstTimeInitLogin().isMal();
                } else {
                    activity2.setMAL(true);
                    activity2.getAddAccountLogin().isMal();
                }
                break;
            case R.id.radio_al:
                if (activity1 != null) {
                    activity1.setMAL(false);
                    activity1.getFirstTimeInitLogin().isMal();
                } else {
                    activity2.setMAL(false);
                    activity2.getAddAccountLogin().isMal();
                }
                break;
        }
    }

    public static FirstTimeInitChoose newInstance(AddAccount addAccount) {
        FirstTimeInitChoose fragment = new FirstTimeInitChoose();
        fragment.activity2 = addAccount;
        return fragment;
    }
}
