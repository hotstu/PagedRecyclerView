package github.hotstu.pagedrecyclerview.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        MySwipeRefreshLayout swipeRefreshLayout = new MySwipeRefreshLayout(this);
        RecyclerView recyclerView = new RecyclerView(this);
        swipeRefreshLayout.addView(recyclerView);
//
//        swipeRefreshLayout.setOnRefreshListener(new NestSwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                swipeRefreshLayout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 3000);
//            }
//        });
        setContentView(swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MOTypedRecyclerAdapter adapter = new MOTypedRecyclerAdapter();
        adapter.addDelegate(new MOTypedRecyclerAdapter.AdapterDelegate() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(MOTypedRecyclerAdapter adapter, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_scrollview, parent, false);
                return new MOCommonViewHolder(view);
            }

            @Override
            public void onBindViewHolder(MOTypedRecyclerAdapter adapter, RecyclerView.ViewHolder holder, Object data) {
            }

            @Override
            public boolean isDelegateOf(Class<?> clazz, Object item, int position) {
                return true;
            }
        });
        adapter.addItems(Arrays.asList("1", "2", "3", "4"));
        recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutFrozen(true);


        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("BannerLoop", "tick");
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                assert adapter != null;
                if (firstVisibleItemPosition + 1 < adapter.getItemCount()) {
                    layoutManager.smoothScrollToPosition(recyclerView,null, firstVisibleItemPosition + 1);
                } else {
                    layoutManager.scrollToPosition(0);
                }
                recyclerView.postDelayed(this, 3000);
            }
        };
        //recyclerView.postDelayed(task, 3000);
    }
}
