package com.aqueleangelo.myfirstandroidapp;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class MainActivity extends AppCompatActivity implements EditDialog.EditDialogListener {
    private ArrayList<ListItemCard> mItemList;

    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Menu mMenu;

    private Button buttonInsert;
    private Button buttonPlay;
    private Button buttonRemove;

    private ItemTouchHelper mItemTouchHelper;

    private boolean shouldAddNewItem = true;
    private int lastItemClicked;

    private static final int MAX_SECONDS = 300;
    private static final int MIN_SECONDS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createItemList();
        buildRecyclerView();
        setButtons();

        mItemTouchHelper = new ItemTouchHelper(simpleCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


        //ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        //itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    // Create and show menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;

        updateCurrentTime();

        return true;
    }

    // Menu bar item options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.time_clock:
                Toast.makeText(this, "TIME",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.option1:
                Toast.makeText(this, "OP1",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.option2:
                Toast.makeText(this, "OP2",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Update the menu timer
    private void updateMenuTimer(int timeSeconds){
        MenuItem timerItem = mMenu.findItem(R.id.time_clock);
        int min_sec[] =  calculateTime(timeSeconds);
        timerItem.setTitle(min_sec[0] + ":" + min_sec[1]);
    }

    // Calculates the minutes & seconds from time in seconds for the timer
    private int[] calculateTime(int time){
        int[] ans = new int[2];
        // Minutes
        ans[0] = time/60;
        // Seconds
        ans[1] = time - ans[0]*60;
        return ans;
    }

    // Update the time from current time listed
    public void updateCurrentTime(){
        int sum = 0;
        for(int i = 0; i < mItemList.size(); i++)
            sum += mItemList.get(i).getTime();
        updateMenuTimer(sum);
    }

    // Changes an item of the mItemList in 'position' with a new 'name' and 'time'
    public void changeItem(int position, String name, int time){
        if(position >= 0 | position <= mItemList.size()) {
            mItemList.get(position).changeTopText(name);
            mItemList.get(position).changeTime(time);
            notifyAdapterAndUpdateTimer(position);
        }
    }

    // Inserts a new item at 'position'
    public void insertItem(int position, String name, int time){
        if(position >= 0 | position <= mItemList.size()){
            mItemList.add(position, new ListItemCard(name, time, false));
            notifyAdapterAndUpdateTimer(position);
        }
    }

    // Removes item at 'position'
    public void removeItem(int position){
        if(position >= 0 | position <= mItemList.size()) {
            mItemList.remove(position);
            notifyAdapterAndUpdateTimer(position);
        }
    }

    // Changes the state of selection of item in 'position' to 'isChecked'
    public void changeSelection(int position, boolean isChecked){
        if(position >= 0 | position <= mItemList.size())
            mItemList.get(position).changeChecked(isChecked);
    }

    // Notifier for adapter (add, removal and change of items)
    // Updater for the timer in the top right corner
    private void notifyAdapterAndUpdateTimer(int position){
        mAdapter.notifyItemRemoved(position);
        updateCurrentTime();
    }

    public void createItemList(){
        mItemList = new ArrayList<>();
        mItemList.add(new ListItemCard("Flexão normal", 20, false));
        mItemList.add(new ListItemCard("Prancha normal", 40, false));
        mItemList.add(new ListItemCard("Agachamento normal", 15, false));

    }

    public void buildRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ListAdapter(mItemList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new ListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                shouldAddNewItem = false;
                lastItemClicked = position;
                openDialog();
            }

            @Override
            public void OnCheckChange(int position, boolean isChecked) {
                changeSelection(position, isChecked);
            }

            @Override
            public void OnStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
    }

    public void setButtons(){
        buttonInsert = findViewById(R.id.button_insert);
        buttonPlay = findViewById(R.id.button_play);
        buttonRemove = findViewById(R.id.button_remove);

        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldAddNewItem = true;
                openDialog();
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get all the checked indexes on the item list, uncheck all and remove them from recyclerView
                // Removal must be Bottom -> Top

                List<Integer> cheks = new ArrayList<>();
                for(int i = 0; i < mItemList.size(); i++){
                    ListItemCard current = mItemList.get(i);
                    if(current.isChecked()){
                        cheks.add(i);
                        current.changeChecked(false);
                    }
                }
                for(int i = cheks.size() - 1; i >= 0; i--)
                    removeItem(cheks.get(i));
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"you pressed play", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void openDialog(){
        EditDialog editDialog = new EditDialog();
        editDialog.show(getSupportFragmentManager(),"edit_dialog");
    }

    @Override
    public void applyTexts(String activityName, String time) {

        // Check if name & time are not blank
        // Clamp the time and display, creating or changing an item

        if(activityName.length() > 0 & time.length() > 0){
            int clampedTimeInt = clamp(Integer.parseInt(time));
            if(shouldAddNewItem)
                insertItem(mItemList.size(), activityName, clampedTimeInt);
            else
                changeItem(lastItemClicked, activityName, clampedTimeInt);
        }
    }

    ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int position_dragged = viewHolder.getAdapterPosition();
            int position_target = target.getAdapterPosition();

            Collections.swap(mItemList, position_dragged, position_target);
            mAdapter.notifyItemMoved(position_dragged, position_target);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if(actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                viewHolder.itemView.setAlpha(0.5f);
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
        }
    };

    public static int clamp(int val) {
        return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, val));
    }
}
