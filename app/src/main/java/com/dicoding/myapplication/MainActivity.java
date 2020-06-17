package com.dicoding.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 100;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String email = user != null ? user.getEmail() : null;
    String title = "";
    String drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Welcome " + user.getDisplayName(),Toast.LENGTH_LONG).show();

            displayChatMessages();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.input);
                FirebaseDatabase.getInstance()
                        .getReference().push().setValue(new ChatMessage(input.getText().toString(),
                        user.getDisplayName()));

                input.setText("");
            }
        });
    }

    private void displayChatMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListAdapter<ChatMessage> adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.message,
                FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy (HH:mm)");
                messageTime.setText(dateFormat.format(model.getMessageTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (email != null){
            if (email.equals("jarjit@mail.com")) {
                title = "Ismail bin Mail";
                drawable ="https://api.adorable.io/avatars/160/ismail@mail.com.png";
            }else {
                title = "Jarjit Singh";
                drawable ="https://api.adorable.io/avatars/160/jarjit@mail.com.png";
            }
        }
        Glide.with(this).load(drawable).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                Bitmap bitmap = ((GlideBitmapDrawable)resource).getBitmap();
                Drawable drawable = new GlideBitmapDrawable(getResources(),bitmap);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setIcon(drawable);
                getSupportActionBar().setTitle(title);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                recreate();
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",Toast.LENGTH_LONG).show();
                displayChatMessages();
            } else {
                Toast.makeText(this, "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG).show();

                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,"You has been signed out.",
                                    Toast.LENGTH_LONG).show();

                            finish();
                        }
                    });
        }
        return true;
    }
}
