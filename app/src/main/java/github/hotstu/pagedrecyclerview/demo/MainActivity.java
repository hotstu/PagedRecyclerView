package github.hotstu.pagedrecyclerview.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

import github.hotstu.naiue.widget.recycler.MOCommonViewHolder;
import github.hotstu.naiue.widget.recycler.MOTypedRecyclerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = new RecyclerView(this);
        setContentView(recyclerView);
        PagedLinearLayoutManager layout = new PagedLinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);
        MOTypedRecyclerAdapter adapter = new MOTypedRecyclerAdapter();
        adapter.addDelegate(new MOTypedRecyclerAdapter.AdapterDelegate() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(MOTypedRecyclerAdapter adapter, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_scrollview, parent, false);
                return new MOCommonViewHolder(view);
            }

            @Override
            public void onBindViewHolder(MOTypedRecyclerAdapter adapter, RecyclerView.ViewHolder holder, Object data) {
                if (holder.itemView instanceof BouncingFrameLayout) {
                    BouncingFrameLayout itemView = (BouncingFrameLayout) holder.itemView;
                    itemView.setBouncingEventListener(type -> {
                        layout.forwardOrBackPage(recyclerView, type);
                    });
                }
            }

            @Override
            public boolean isDelegateOf(Class<?> clazz, Object item, int position) {
                return position % 2 == 0;
            }
        });

        adapter.addDelegate(new MOTypedRecyclerAdapter.AdapterDelegate() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(MOTypedRecyclerAdapter adapter, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nestedlist, parent, false);
                RecyclerView list = view.findViewById(R.id.list);
                MOCommonViewHolder holder = new MOCommonViewHolder(view);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(parent.getContext());
                list.setLayoutManager(linearLayoutManager);
                MOTypedRecyclerAdapter adapter1 = new MOTypedRecyclerAdapter();
                adapter1.addDelegate(new MOTypedRecyclerAdapter.AdapterDelegate() {
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(MOTypedRecyclerAdapter adapter, ViewGroup parent) {
                        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_simple, parent, false);
                        return new MOCommonViewHolder(inflate);
                    }

                    @Override
                    public void onBindViewHolder(MOTypedRecyclerAdapter adapter, RecyclerView.ViewHolder holder, Object data) {
                        ((TextView) holder.itemView).setText("item:" + data);
                    }

                    @Override
                    public boolean isDelegateOf(Class<?> clazz, Object item, int position) {
                        return true;
                    }
                });
                list.setAdapter(adapter1);
                adapter1.addItems(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16));
                return holder;
            }

            @Override
            public void onBindViewHolder(MOTypedRecyclerAdapter adapter, RecyclerView.ViewHolder holder, Object data) {
                if (holder.itemView instanceof BouncingFrameLayout) {
                    BouncingFrameLayout itemView = (BouncingFrameLayout) holder.itemView;
                    itemView.setBouncingEventListener(type -> {
                        int firstVisibleItemPosition = layout.findFirstVisibleItemPosition();
                        int target = firstVisibleItemPosition;
                        if (type == BouncingFrameLayout.FLAG_REACH_BOTTOM) {
                            target += 1;
                        } else if (type == BouncingFrameLayout.FLAG_REACH_TOP) {
                            target -= 1;
                        }

                        if (target < 0) {
                            target = 0;
                        }
                        layout.smoothScrollToPosition(recyclerView, null, target);

                    });
                }
            }

            @Override
            public boolean isDelegateOf(Class<?> clazz, Object item, int position) {
                return position % 2 == 1;
            }
        });
        adapter.addItems(Arrays.asList("1", "2", "3", "4"));
        recyclerView.setAdapter(adapter);
        //recyclerView.postDelayed(task, 3000);
    }
}
