package io.agora.scene.rtegame.ui.room.game;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.bean.AgoraGame;
import io.agora.scene.rtegame.databinding.GameItemGameBinding;

public class ItemGameHolder extends BaseRecyclerViewAdapter.BaseViewHolder<GameItemGameBinding, AgoraGame> {

    public ItemGameHolder(@NonNull GameItemGameBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable AgoraGame data, int selectedIndex) {
        if (data == null) return;
        mBinding.getRoot().setText(data.getGameName());
        mBinding.getRoot().setOnClickListener(this::onItemClick);
        if (TextUtils.isEmpty(data.getIconUrl())) {
            mBinding.getRoot().setIcon(ContextCompat.getDrawable(mBinding.getRoot().getContext(), R.drawable.game_ic_game_1));
        } else {
            Glide.with(itemView).load(data.getIconUrl())
                    .transform(new RoundedCorners((int) BaseUtil.dp2px(12)))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mBinding.getRoot().post(() -> mBinding.getRoot().setIcon(resource));
                            return false;
                        }
                    }).submit();
        }
    }
}
