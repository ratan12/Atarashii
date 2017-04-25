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
    private FirstTimeInit activity;

    public static FirstTimeInitChoose newInstance(FirstTimeInit activity) {
        FirstTimeInitChoose fragment = new FirstTimeInitChoose();
        fragment.activity = activity;
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
                activity.isMAL = true;
                activity.firstTimeInitLogin.isMal();
                break;
            case R.id.radio_al:
                activity.isMAL = false;
                activity.firstTimeInitLogin.isMal();
                break;
        }
    }
}
