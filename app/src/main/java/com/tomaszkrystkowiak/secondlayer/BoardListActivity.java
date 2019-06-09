package com.tomaszkrystkowiak.secondlayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class BoardListActivity extends AppCompatActivity {

    private ArrayList<Board> boardArray;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "boards").build();
        DbboardsAsyncTask dbRoutesAsyncTask = new DbboardsAsyncTask();
        dbRoutesAsyncTask.execute();
        BoardListAdapter adapter = new BoardListAdapter(this, boardArray);
        ListView listView = (ListView) findViewById(R.id.board_list );
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
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_board, parent, false);
            }
            // Lookup view for data population
            TextView tvTitle = (TextView) convertView.findViewById(R.id.board_title);
            TextView tvDate = (TextView) convertView.findViewById(R.id.board_date);
            // Populate the data into the template view using the data object
            tvTitle.setText(board.title);
            tvDate.setText(board.date.toString());
            // Return the completed view to render on screen
            return convertView;
        }

    }

    static class ViewHolder {
        TextView title;
        TextView creationDate;
    }

    private class DbboardsAsyncTask extends AsyncTask<Void , Void, List<Board>> {


        @Override
        protected  List<Board> doInBackground(Void...voids) {

            List<Board> boardList = db.boardDao().getAllUserBoards(getResources().getString(R.string.user_name));
            if(boardList.isEmpty()){
                return null;
            }
            else {
                return boardList;
            }
        }

        @Override
        protected void onPostExecute(List<Board> boardList) {
            if(boardList != null) {
                for( Board board: boardList){
                    boardArray.add(board);
                }
            }else{

            }

        }

    }
}
