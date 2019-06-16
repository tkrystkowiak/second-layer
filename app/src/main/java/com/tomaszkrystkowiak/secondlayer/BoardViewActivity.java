package com.tomaszkrystkowiak.secondlayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.google.ar.schemas.lull.OptionalBool.False;

public class BoardViewActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ViewRenderable boardRenderable;
    private Button addButton;
    private Button messageButton;
    private Button saveButton;
    private TextView textView;
    private String boardTitle;
    private AppDatabase db;
    private final static String TAG = "BoardViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.board_fragment);
        ViewRenderable.builder()
                .setView(this, R.layout.board)
                .build()
                .thenAccept(renderable -> boardRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load board renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 50);
                            toast.show();
                            return null;
                        });
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (boardRenderable == null) {
                        return;
                    }
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode board = new TransformableNode(arFragment.getTransformationSystem());
                    board.setParent(anchorNode);
                    board.setRenderable(boardRenderable);
                    board.select();

                    addButton = boardRenderable.getView().findViewById(R.id.button_add);
                    addButton.setVisibility(View.GONE);
                    messageButton = boardRenderable.getView().findViewById(R.id.button_message);

                    saveButton = boardRenderable.getView().findViewById(R.id.button_save);
                    saveButton.setOnClickListener(new SaveButtonClick());
                    textView = boardRenderable.getView().findViewById(R.id.title_textView);
                    textView.setText(boardTitle);

                    arFragment.setOnTapArPlaneListener(null);

                });

    }

    public void saveBoard() {
        DbBoardSavingAsyncTask dbBoardSavingAsyncTask = new DbBoardSavingAsyncTask();
        dbBoardSavingAsyncTask.execute();
    }

    public Board prepareBoardToSave(){
        Board toSave = new Board();
        toSave.creator = getResources().getString(R.string.user_name);
        toSave.title = boardTitle;
        toSave.date = Calendar.getInstance().getTime();
        toSave.messages = new ArrayList<>();
        return toSave;
    }

    public void startSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save the board?");
        builder.setMessage("The board will be saved.");
        builder.setPositiveButton("OK", (dialog, which) -> saveBoard());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private class MessageListAdapter extends ArrayAdapter<Message> {
        public MessageListAdapter(Context context, ArrayList<Message> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Message message = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            }
            // Lookup view for data population
            TextView tvTitle = convertView.findViewById(R.id.board_title);
            TextView tvDate = convertView.findViewById(R.id.board_date);
            TextView tvMessages = convertView.findViewById(R.id.board_messages);
            Button delete = convertView.findViewById(R.id.delete_button);
            View.OnClickListener deleteButtonClick = new BoardListActivity.DeleteButtonClick();
            ((BoardListActivity.DeleteButtonClick) deleteButtonClick).setBoardToDelete(board);
            delete.setOnClickListener(deleteButtonClick);
            // Populate the data into the template view using the data object
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
            tvTitle.setText(board.title);
            tvDate.setText(dt.format(board.date));
            tvMessages.setText(String.valueOf(board.messages.size()));


            // Return the completed view to render on screen
            return convertView;
        }

    private class MessageButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            startNewMessageDialog();
        }
    }

    private class SaveButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            startSaveDialog();
        }
    }

    private class DbBoardSavingAsyncTask extends AsyncTask<Void, Void, Board> {


        @Override
        protected Board doInBackground(Void...voids) {

            Board toSave = prepareBoardToSave();
            db.boardDao().insert(toSave);
            return toSave;

        }

    }
}
