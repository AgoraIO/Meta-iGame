package io.agora.scene.rtegame;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;

import io.agora.example.base.BaseActivity;
import io.agora.scene.rtegame.databinding.GameActivityMainBinding;
import io.agora.scene.rtegame.util.GameUtil;

public class MainActivity extends BaseActivity<GameActivityMainBinding> {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameUtil.getAndroidViewModel(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

}