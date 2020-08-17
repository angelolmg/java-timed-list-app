package com.aqueleangelo.myfirstandroidapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class MainActivity extends AppCompatActivity implements EditDialog.EditDialogListener {
    private ArrayList<ListItemCard> mItemList;

    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private MenuItem mTimer;
    private ListTimer mListTimer;

    private Button buttonInsert;
    private Button buttonPlay;
    private Button buttonRemove;

    private ItemTouchHelper mItemTouchHelper;
    private CountDownTimer mCountDownTimer;

    private boolean shouldAddNewItem = true;
    private boolean timeRunning = false;
    private int lastItemClicked;
    private long timeLeft;

    private static final int MAX_SECONDS = 300;
    private static final int MIN_SECONDS = 5;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TASK_LIST = "task_list";

    public static final int ACCLIMATION_TIME = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //createItemList();
        loadData();
        buildRecyclerView();
        setButtons();

        mItemTouchHelper = new ItemTouchHelper(simpleCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    // Create and show menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mTimer = menu.findItem(R.id.time_clock);

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
    private void updateMenuTimerText(long timeSeconds){
        int minutes = (int) (timeSeconds / 1000) / 60;
        int seconds = (int) (timeSeconds / 1000) % 60;

        String timeStamp = String.format("%02d:%02d", minutes, seconds);
        mTimer.setTitle(timeStamp);
    }

    // Update the time from current time listed
    public void updateCurrentTime(){
        if(mItemList.size() > 0) {
            int sum = 0;
            for (ListItemCard listItemCard : mItemList) sum += listItemCard.getTime();
            timeLeft = sum * 2;
            timeLeft += ACCLIMATION_TIME;
            timeLeft -= mItemList.get(mItemList.size()-1).getTime();
            timeLeft *= 1000;
        } else {
            timeLeft = 0;
        }

        updateMenuTimerText(timeLeft);
        saveData();
    }

    // Changes an item of the mItemList in 'position' with a new 'name' and 'time'
    public void changeItem(int position, String name, int time){
        if(position >= 0 & position <= mItemList.size()) {
            mItemList.get(position).changeTopText(name);
            mItemList.get(position).changeTime(time);
            mAdapter.notifyItemChanged(position);
            updateCurrentTime();
            saveData();
        }
    }

    // Inserts a new item at 'position'
    public void insertItem(int position, String name, int time){
        if(position >= 0 & position <= mItemList.size()){
            mItemList.add(position, new ListItemCard(name, time, false));
            mAdapter.notifyItemInserted(position);
            updateCurrentTime();
            saveData();
        }
    }

    // Removes item at 'position'
    public void removeItem(int position){
        if(position >= 0 & position <= mItemList.size()) {
            mItemList.remove(position);
            mAdapter.notifyItemRemoved(position);
            updateCurrentTime();
            saveData();
        }
    }

    // Changes the state of selection of item in 'position' to 'isChecked'
    public void changeSelection(int position, boolean isChecked){
        if(position >= 0 & position <= mItemList.size()){
            mItemList.get(position).changeChecked(isChecked);
            saveData();
        }
    }


    public void createItemList(){
        mItemList = new ArrayList<>();
        mItemList.add(new ListItemCard("Flexão normal", 5, false));
        mItemList.add(new ListItemCard("Prancha normal", 10, false));
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
                if(timeRunning)
                    return;

                shouldAddNewItem = false;
                lastItemClicked = position;
                ListItemCard currentItem = mItemList.get(position);
                openDialog(currentItem.getTopText(), String.valueOf(currentItem.getTime()));
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
                if(timeRunning)
                    return;

                shouldAddNewItem = true;
                openDialog("","");
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get all the checked indexes on the item list, uncheck all and remove them from recyclerView
                // Removal must be Bottom -> Top

                ArrayList<Integer> checks = new ArrayList<>();
                for(int i = 0; i < mItemList.size(); i++){
                    ListItemCard current = mItemList.get(i);
                    if(current.isChecked()){
                        checks.add(i);
                        current.changeChecked(false);
                    }
                }
                for(int i = checks.size() - 1; i >= 0; i--)
                    removeItem(checks.get(i));
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeRunning){
                    resetTimer();
                } else{
                    startTimer();
                }
            }
        });
    }

    private void startTimer(){
        mListTimer = new ListTimer(getActivityTimes(), ACCLIMATION_TIME);
        Log.d("LOGME", String.valueOf(mListTimer.getTimerTimes()));
        Log.d("LOGME", String.valueOf(mListTimer.getActivityNumbs()));

        mCountDownTimer = new CountDownTimer(timeLeft, 1000) {
            long totalTime = timeLeft;
            int lastEventIndex = -1;

            @Override
            public void onTick(long l) {
                timeLeft = l;
                updateMenuTimerText(timeLeft);

                int timeToGive = (int) ((totalTime/1000) - (timeLeft/1000));
                int eventIndex = mListTimer.checkForEvent(timeToGive);
                if(eventIndex >= 0){
                    if(eventIndex == lastEventIndex){
                        mLayoutManager.getChildAt(eventIndex).setBackgroundColor(Color.YELLOW);
                    } else{
                        mLayoutManager.getChildAt(eventIndex).setBackgroundColor(Color.GREEN);
                        if(lastEventIndex >= 0)
                            mLayoutManager.getChildAt(lastEventIndex).setBackgroundColor(Color.LTGRAY);
                        lastEventIndex = eventIndex;
                    }
                }
            }

            @Override
            public void onFinish() {
                resetTimer();
            }
        }.start();

        buttonPlay.setText("Stop");
        buttonInsert.setEnabled(false);
        buttonRemove.setEnabled(false);
        timeRunning = true;
    }

    private ArrayList<Integer> getActivityTimes() {
        ArrayList<Integer> timeList = new ArrayList<>();
        for (ListItemCard itemList: mItemList)
            timeList.add(itemList.getTime());
        return timeList;
    }

    private void resetTimer(){
        mCountDownTimer.cancel();
        updateCurrentTime();

        // Sets the bg color for all view holders to white
        for(int i = 0; i < mItemList.size(); i++)
            mLayoutManager.getChildAt(i).setBackgroundColor(Color.WHITE);

        buttonPlay.setText("Play");
        buttonInsert.setEnabled(true);
        buttonRemove.setEnabled(true);
        timeRunning = false;
    }

    public void openDialog(String currentName, String currentTime){
        EditDialog editDialog = new EditDialog(currentName, currentTime);
        editDialog.show(getSupportFragmentManager(),"edit_dialog");
    }

    // Check if name & time are not blank
    // Then handle invalid number formats (like really big ones)
    // Clamp the time and display, creating or changing an item
    @Override
    public void applyTexts(String activityName, String time) {
        if(activityName.length() > 0 & time.length() > 0){
            int timeInt;
            try {
                timeInt = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                timeInt = MAX_SECONDS;
            }

            int clampedTimeInt = clamp(timeInt);
            if(shouldAddNewItem){
                insertItem(mItemList.size(), activityName, clampedTimeInt);
            }else{
                changeItem(lastItemClicked, activityName, clampedTimeInt);
            }
        }
    }

    ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if(timeRunning)
                return ItemTouchHelper.ACTION_STATE_IDLE;
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int position_dragged = viewHolder.getAdapterPosition();
            int position_target = target.getAdapterPosition();

            Collections.swap(mItemList, position_dragged, position_target);
            mAdapter.notifyItemMoved(position_dragged, position_target);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                assert viewHolder != null;
                viewHolder.itemView.setAlpha(0.5f);
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
            updateCurrentTime();
        }
    };

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json= gson.toJson(mItemList);
        editor.putString(TASK_LIST, json);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TASK_LIST, null);
        Type type = new TypeToken<ArrayList<ListItemCard>>(){}.getType();
        mItemList = gson.fromJson(json, type);

        if(mItemList == null){
            mItemList = new ArrayList<>();
        }
    }

    public static int clamp(int val) {
        return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, val));
    }
    public void showList(){
        for (ListItemCard l: mItemList)
            Log.d("LOGME", l.getTopText() + " " + l.getTime());
    }
}
