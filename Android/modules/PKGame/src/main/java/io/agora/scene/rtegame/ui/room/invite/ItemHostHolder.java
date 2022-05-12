package io.agora.scene.rtegame.ui.room.invite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.databinding.GameItemDialogHostBinding;
import io.agora.scene.rtegame.util.GameUtil;

public class ItemHostHolder extends BaseRecyclerViewAdapter.BaseViewHolder<GameItemDialogHostBinding, RoomInfo> {
    public ItemHostHolder(@NonNull GameItemDialogHostBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable RoomInfo roomInfo, int selectedIndex) {
        if (roomInfo != null){
            mBinding.avatarItemHost.setImageResource(GameUtil.getBgdByRoomBgdId(roomInfo.getBackgroundId()));
            mBinding.nameItemHost.setText(itemView.getContext().getString(R.string.game_format_two_string, roomInfo.getRoomName() , roomInfo.getId()));
            mBinding.buttonItemHost.setOnClickListener(this::onItemClick);
        }
    }
}