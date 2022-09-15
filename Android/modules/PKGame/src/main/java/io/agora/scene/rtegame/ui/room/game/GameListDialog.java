package io.agora.scene.rtegame.ui.room.game;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.example.base.BaseBottomSheetDialogFragment;
import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.OnItemClickListener;
import io.agora.scene.rtegame.bean.AgoraGame;
import io.agora.scene.rtegame.databinding.GameDialogGameListBinding;
import io.agora.scene.rtegame.databinding.GameItemGameBinding;
import io.agora.scene.rtegame.ui.room.RoomViewModel;
import io.agora.scene.rtegame.ui.room.invite.HostListDialog;
import io.agora.scene.rtegame.util.GameUtil;

public class GameListDialog extends BaseBottomSheetDialogFragment<GameDialogGameListBinding> implements OnItemClickListener<AgoraGame> {
    public static final String TAG = "GameListDialog";

    private BaseRecyclerViewAdapter<GameItemGameBinding, AgoraGame, ItemGameHolder> mAdapter;

    RoomViewModel roomViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomViewModel = GameUtil.getViewModel(RoomViewModel.class, requireParentFragment());
        WindowCompat.setDecorFitsSystemWindows(requireDialog().getWindow(), false);
        initView();

        roomViewModel.gameList().observe(getViewLifecycleOwner(), agoraGames -> {
            mAdapter.addItems(agoraGames);
        });

        roomViewModel.fetchGameList();
    }

    private void initView() {
        GameUtil.setBottomDialogBackground(mBinding.getRoot());
        mAdapter = new BaseRecyclerViewAdapter<>(null, this, ItemGameHolder.class);
        mBinding.recyclerViewDialogGameList.setAdapter(mAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(requireDialog().getWindow().getDecorView(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mBinding.getRoot().setPadding(inset.left, 0, inset.right, inset.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(requireDialog().getWindow().getDecorView());
        if (controller != null) {
            boolean isNightMode = GameUtil.isNightMode(getResources().getConfiguration());
            controller.setAppearanceLightStatusBars(!isNightMode);
            controller.setAppearanceLightNavigationBars(!isNightMode);
        }
        mBinding.recyclerViewDialogGameList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) mBinding.recyclerViewDialogGameList.getLayoutManager();
                if (null == manager) return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItem = manager.getItemCount();
                    if (lastVisibleItem == (totalItem - 1)) {
                        //当滑动到最后一个时，加载更多数据
                        roomViewModel.fetchGameListMore();
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(@NonNull AgoraGame data, @NonNull View view, int position, long viewType) {
        showHostListDialog(data.getGameId(), data.getVendorId());
    }

    private void showHostListDialog(String gameId, String vendorid) {
        dismiss();
        new HostListDialog(gameId, vendorid).show(getParentFragmentManager(), HostListDialog.TAG);
    }

}
