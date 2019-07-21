package de.parkitny.fit.myfit.app.ui.account;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.events.FragmentType;
import de.parkitny.fit.myfit.app.events.LockDrawerEvent;
import de.parkitny.fit.myfit.app.events.StartFragmentEvent;
import de.parkitny.fit.myfit.app.ui.common.Bar;

@SuppressLint("ValidFragment")
@Bar(Position = 4)
public class UserLogin extends Fragment {

    private AppCompatActivity activity;

    public UserLogin(AppCompatActivity appCompatActivity) {
        this.activity = appCompatActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rzp_user_login, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().post(new LockDrawerEvent(true));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().post(new LockDrawerEvent(false));
    }

    @OnClick(R.id.signInButton)
    protected void onSignIn() {

        EventBus.getDefault().post(new StartFragmentEvent(FragmentType.WORKOUTS, new Bundle()));
    }
}
