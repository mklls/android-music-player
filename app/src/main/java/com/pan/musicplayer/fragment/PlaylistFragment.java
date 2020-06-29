package com.pan.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pan.musicplayer.R;
import com.pan.musicplayer.activity.MainActivity;
import com.pan.musicplayer.activity.PlaylistActivity;
import com.pan.musicplayer.adapter.MyPlaylistViewAdapter;
import com.pan.musicplayer.service.AudioService;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistFragment extends Fragment {
    private AudioService audioService;
    private ListView list;
    private MainActivity activity;
    private MyPlaylistViewAdapter adapter;
    private ExtendedFloatingActionButton fab;
    private int currentListIndex;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        audioService = activity.audioService;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        list = v.findViewById(R.id.list);
        fab = v.findViewById(R.id.fab);
        return v;
    }

    @SuppressWarnings("ConstantConditions")
    public void newPlaylist() {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View v = getLayoutInflater().inflate(R.layout.dialog_new_playlist, null);
        final TextInputLayout l = v.findViewById(R.id.title);
        l.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = l.getEditText().getText().toString().trim();
                if (title.isEmpty()) return;

                if (audioService.localResourceHelper.newPlaylist(title)) {
                    update();
                    dialog.cancel();
                } else {
                    l.setErrorEnabled(true);
                    l.setError("名称不可用");
                }
            }
        });
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextInputLayout) l).setErrorEnabled(false);
            }
        });
        dialog.setView(v);
        dialog.show();
    }

        private void renamePlaylist() {
            final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
            final View inputLayout = getLayoutInflater().inflate(R.layout.dialog_new_playlist, null);
            ((TextInputLayout) inputLayout).setEndIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = ((TextInputLayout) inputLayout).getEditText().getText().toString().trim();
                    if (title.isEmpty()) return;

                    String id = audioService.localResourceHelper.playlists.get(currentListIndex).getId();
                    if (audioService.localResourceHelper.changePlaylistTitle(id, title)) {
                        update();
                        dialog.cancel();
                    } else {
                        ((TextInputLayout) inputLayout).setErrorEnabled(true);
                        ((TextInputLayout) inputLayout).setError("名称不可用");
                    }
                }
            });
            ((TextInputLayout) inputLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TextInputLayout) inputLayout).setErrorEnabled(false);
                }
            });
            dialog.setView(inputLayout);
            dialog.show();
        }

        private void handleOnItemClick(final int position) {
            switch (position) {
                case 0:
                    renamePlaylist();
                    break;
                case 1:
                    if (currentListIndex == 0) {
                        Snackbar
                            .make(list, "无法删除默认列表",
                                    BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        String id = audioService
                                .localResourceHelper
                                .playlists
                                .get(currentListIndex)
                                .getId();
                        audioService.localResourceHelper.removePlaylist(id);
                    }
                    break;
            }
            update();
        }
    @SuppressWarnings("ConstantConditions")
    private void showBottomDialog() {
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_playlist_actions, null);
        ListView actions = v.findViewById(R.id.action);

        int[] icons = {R.drawable.edit, R.drawable.delete};
        String[] titles = {"重命名", "删除"};
        ArrayList<HashMap<String, Object>> items = new ArrayList<>();

        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> temp = new HashMap<>();
            temp.put("icon", icons[i]);
            temp.put("title", titles[i]);
            items.add(temp);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(
                getContext(),
                items,
                R.layout.item_bottom_sheet_action,
                new String[]{"icon", "title"},
                new int[]{R.id.icon, R.id.title});

        actions.setAdapter(simpleAdapter);

        actions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleOnItemClick(position);
                dialog.cancel();
            }
        });

        dialog.setContentView(v);

        dialog.show();
    }

    private void update() {
        if (audioService == null) return;
        adapter = new MyPlaylistViewAdapter(getContext(),
                audioService.localResourceHelper.playlists);
        list.setAdapter(adapter);

        adapter.setOnEndIconClickListener(new MyPlaylistViewAdapter.OnEndIconClickListener() {
            @Override
            public void OnClick(int position) {
                currentListIndex = position;
                showBottomDialog();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), PlaylistActivity.class);
                i.putExtra("index", position);
                i.putExtra(PlaylistActivity.VIEW_TYPE,
                        PlaylistActivity.VIEW_TYPE_PLAYLIST);
                startActivity(i);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPlaylist();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("on Start");
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}
