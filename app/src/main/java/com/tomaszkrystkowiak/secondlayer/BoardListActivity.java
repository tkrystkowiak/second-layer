package com.tomaszkrystkowiak.secondlayer;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class BoardListActivity extends AppCompatActivity {

    private final static String TAG = "BoardListActivity";
    private ArrayList<Board> boardArray;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);
        boardArray = new ArrayList<>();
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "boards").build();
        DbboardsAsyncTask dbRoutesAsyncTask = new DbboardsAsyncTask();
        dbRoutesAsyncTask.execute();

    }

    private void setAdapter(){
        BoardListAdapter adapter = new BoardListAdapter(this, boardArray);
        ListView listView = findViewById(R.id.board_list );
        listView.setAdapter(adapter);
    }

    private class BoardListAdapter extends ArrayAdapter<Board>{
        public BoardListAdapter(Context context, ArrayList<Board> boards) {
            super(context, 0, boards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Board board = getItem(position);
            Log.i(TAG, "Board readed" + board.creator + ", " +board.date);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_board, parent, false);
            }
            // Lookup view for data population
            TextView tvTitle = convertView.findViewById(R.id.board_title);
            TextView tvDate = convertView.findViewById(R.id.board_date);
            TextView tvMessages = convertView.findViewById(R.id.board_messages);
            // Populate the data into the template view using the data object
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
            tvTitle.setText(board.title);
            tvDate.setText(dt.format(board.date));
            tvMessages.setText(String.valueOf(board.messages.size()));
            // Return the completed view to render on screen
            return convertView;
        }

    }


    private class DbboardsAsyncTask extends AsyncTask<Void , Void, List<Board>> {


        @Override
        protected  List<Board> doInBackground(Void...voids) {

            List<Board> boardList = db.boardDao().getAllUserBoards(getResources().getString(R.string.user_name));
            if(boardList.isEmpty()){
                return null;
            }
            else {
                Log.i(TAG, "Number of readed items " + boardList.size());
                return boardList;
            }
        }

        @Override
        protected void onPostExecute(List<Board> boardList) {
            if(boardList != null) {
                for( Board board: boardList){
                    boardArray.add(board);
                }
                setAdapter();
            }
        }

    }
}
