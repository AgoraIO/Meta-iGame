package io.agora.scene.rtegame.ui.list;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.scene.rtegame.GlobalViewModel;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.databinding.GameItemRoomListBinding;
import io.agora.scene.rtegame.util.GameConstants;
import io.agora.scene.rtegame.util.GameUtil;

public class RoomListHolder extends BaseRecyclerViewAdapter.BaseViewHolder<GameItemRoomListBinding, RoomInfo> {
    public RoomListHolder(@NonNull GameItemRoomListBinding mBinding) {
        super(mBinding);
    }

    @Override
    public void binding(@Nullable RoomInfo room, int selectedIndex) {
        if (room != null){
            mBinding.ownerItemRoomList.setText(itemView.getContext().getString(R.string.game_room_list_owner, room.getTempUserName()));
            mBinding.nameItemRoomList.setText(itemView.getContext().getString(R.string.game_room_list_name, room.getRoomName(), room.getId()));
            mBinding.bgdItemRoomList.setImageResource(GameUtil.getBgdByRoomBgdId(room.getBackgroundId()));

            if (GlobalViewModel.localUser != null){
                boolean amHost = room.getUserId().equals(GlobalViewModel.localUser.getUserId());
                mBinding.ownerItemRoomList.setTextColor(amHost ? ColorStateList.valueOf(Color.MAGENTA) : mBinding.nameItemRoomList.getTextColors());
            }
        }
    }
}
