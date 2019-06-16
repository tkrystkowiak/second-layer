package com.tomaszkrystkowiak.secondlayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BoardViewActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ViewRenderable boardRenderable;
    private Button addButton;
    private Button messageButton;
    private Button saveButton;
    private TextView textView;
    private String boardTitle;
    private ArrayList<Message> messageslist;
    private Board mainBoard;
    private AppDatabase db;
    private ListView listView;
    private MessageListAdapter adapter;

    private final static String TAG = "BoardViewActivity";
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        boardTitle = getIntent().getStringExtra("title");
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "boards").build();
        Log.i(TAG,"readedBoardTitle: " + boardTitle);
        DbBoardReadingAsyncTask readingAsyncTask = new DbBoardReadingAsyncTask();
        readingAsyncTask.execute(boardTitle);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.board_fragment2);
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

                    saveButton = boardRenderable.getView().findViewById(R.id.button_save);
                    saveButton.setOnClickListener(new SaveButtonClick());

                    messageButton = boardRenderable.getView().findViewById(R.id.button_message);
                    messageButton.setOnClickListener(new MessageButtonClick());

                    textView = boardRenderable.getView().findViewById(R.id.title_textView);
                    textView.setText(boardTitle);

                    listView = boardRenderable.getView().findViewById(R.id.message_list);
                    adapter = new MessageListAdapter(this,mainBoard.messages);
                    listView.setAdapter(adapter);

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

        private ArrayList<Message> localMessages;

        public MessageListAdapter(Context context, ArrayList<Message> messages) {
            super(context, 0, messages);
            localMessages = messages;
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
            TextView tvTitle = convertView.findViewById(R.id.title_message);
            TextView tvContent = convertView.findViewById(R.id.message_content);
            TextView tvAuthor = convertView.findViewById(R.id.author_message);
            EditText likes = convertView.findViewById(R.id.likes_view);
            EditText dislikes = convertView.findViewById(R.id.dislikes_view);
            Button like_button = convertView.findViewById(R.id.imageButton);
            Button dislike_button = convertView.findViewById(R.id.imageButton2);

            // Populate the data into the template view using the data object
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd");
            tvTitle.setText(dt.format(message.getDateOfCreation()));
            tvContent.setText(message.getContent());
            tvAuthor.setText(message.getAutor());
            likes.setText(String.valueOf(message.getNumberOfLikes()));
            dislikes.setText(String.valueOf(message.getNumberOfLikes()));

            // Return the completed view to render on screen
            return convertView;
        }

        public void addMessage(String content){
            Message message = new Message();
            message.setAutor(getResources().getString(R.string.user_name));
            Log.i(TAG,"Message to save: "+content);
            message.setContent(content);
            message.setDateOfCreation(Calendar.getInstance().getTime());
            message.setNumberOfDislikes(0);
            message.setNumberOfLikes(0);
            localMessages.add(message);
        }
    }

    public void startNewMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter title and message");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText messageBox = new EditText(this);

        messageBox.setHint("Message");
        layout.addView(messageBox); // Another add method

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Log.i(TAG,"Message: "+ messageBox.getText().toString());
            adapter.addMessage(messageBox.getText().toString());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private class MessageButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.i(TAG,"message clicked");
                startNewMessageDialog();
        }
    }

    private class SaveButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.i(TAG,"save clicked");
            startSaveDialog();
        }
    }

    private class LikeButtonClick implements View.OnClickListener {

            @Override
            public void onClick(View v) {

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

    private class DbBoardReadingAsyncTask extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... strings) {
            mainBoard = db.boardDao().getByTitle(strings[0]).get(0);
            return null;
        }
    }

}
