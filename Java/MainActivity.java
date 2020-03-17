package com.example.ludoreddit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String username;
    private ArrayList<Message> messages;
    private boolean replying;
    private int replyParent;
    private boolean onCreate;
    private boolean nameEntered;
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("messages");
        myRef.orderByChild("index");

        buildRecyclerView();
        setButtons();

        replying = false;
        onCreate = true;
        nameEntered = false;


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onCreate) {
                    messages.clear();
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        Message newMessage = d.getValue(Message.class);
                        messages.add(newMessage);
                    }

                    onCreate = false;

                    sortMessages();
                    ((Adapter) adapter).update(messages, nameEntered);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    public void sortMessages() {
        ArrayList<Message> sorted = new ArrayList<>();
        int i = 0;
        int size = messages.size();
        while(i < size) {
            for(int m = 0; m < messages.size(); m ++) {
                if(messages.get(m).getIndex() == i) {
                    sorted.add(messages.get(m));
                    messages.remove(m);
                    break;
                }
            }
            i++;
        }
        messages = sorted;
    }


    public void buildRecyclerView() {
        messages = new ArrayList<Message>();
        recyclerView = findViewById(R.id.recyclerViewMessages);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new Adapter(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        ((Adapter) adapter).setOnMessageClickListener(new Adapter.OnMessageClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteMessage(position);
            }

            @Override
            public void onReplyClick(int position) {
                replying = true;
                EditText message = (EditText) findViewById(R.id.editTextInput);
                message.setHint("Reply to " + messages.get(position).getName());
                openKeyboard();

                replyParent = position;
            }

            public void onUpvoteClick(int position) {
                upvote(position);
            }
        });




    }

    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if(view!= null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openKeyboard() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT );

    }



    public void setButtons() {
        final Button showDialog = (Button) findViewById(R.id.showDialogBtn);
        final FloatingActionButton send = (FloatingActionButton) findViewById(R.id.floatingActionBtnSend);
        final EditText messageInput = (EditText) findViewById(R.id.editTextInput);
        messageInput.setEnabled(false);
        messageInput.setInputType(InputType.TYPE_NULL);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replying)
                    addReply(replyParent);
                else
                    addMessage();
            }



        });


        showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.prompt_view, null);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setView(view);
                final EditText input = (EditText) view.findViewById(R.id.nameInputEditText);


                alertBuilder.setCancelable(true).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        username = input.getText().toString();
                        showDialog.setVisibility(view.GONE);
                        send.setVisibility(View.VISIBLE);
                        messageInput.setEnabled(true);
                        messageInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        messageInput.setHintTextColor(Color.GRAY);
                        messageInput.setHint("Create a post!");
                        nameEntered = true;
                        ((Adapter)adapter).update(messages, nameEntered);
                    }
                });
                Dialog dialog = alertBuilder.create();
                dialog.show();

            }
        });
    }

    public void rippleIndexesUp(int index) {
        for(int i = index; i < messages.size(); i++) {
            messages.get(i).incrementIndex();
            myRef.child(messages.get(i).getID()).child("index").setValue(messages.get(i).getIndex());
            if(messages.get(i).getReply()) {
                messages.get(i).incrementParentIndex();
                myRef.child(messages.get(i).getID()).child("parentIndex").setValue(messages.get(i).getParentIndex());
            }
        }
    }

    public void rippleIndexesDown(int index, int pIndex) {
        for(int i = index; i < messages.size(); i++) {
            messages.get(i).decrementIndex();
            myRef.child(messages.get(i).getID()).child("index").setValue(messages.get(i).getIndex());
            if(  (messages.get(i).getParentIndex() !=pIndex) && (messages.get(i).getReply())) {
                messages.get(i).decrementParentIndex();
                myRef.child(messages.get(i).getID()).child("parentIndex").setValue(messages.get(i).getParentIndex());
            }
        }
    }

    public void rippleIndexUp(int index, int stop) {
        for(int i = index; i < stop; i++) {
            messages.get(i).incrementIndex();
            myRef.child(messages.get(i).getID()).child("index").setValue(messages.get(i).getIndex());
        }
    }

    public void rippleIndexDown(int index) {
        for(int i = index; i < messages.size(); i++) {
            messages.get(i).decrementIndex();
            myRef.child(messages.get(i).getID()).child("index").setValue(messages.get(i).getIndex());
        }
    }


    public int findBiggest(List<Message> replies) {
        int max = -1;
        int result = -1;
        for( int i = 0; i < replies.size(); i++) {
            if(replies.get(i).getUpvotes() > max) {
                result = i;
                max = replies.get(i).getUpvotes();
            }
        }
        return result;
    }


    public void swap(int index1, int index2) {
        Message temp = messages.get(index2);
        messages.remove(index2);
        messages.add(index1, temp);
        messages.get(index1).setIndex(index1);
        myRef.child(messages.get(index1).getID()).child("index").setValue(index1);


        //If reply, anticipate that the parentIndex will be index1, and update it
        if(messages.get(index1).getReply()) {
            messages.get(index1).setParentIndex(index1);
            myRef.child(messages.get(index1).getID()).child("parentIndex").setValue(index1);
        }



        rippleIndexUp(index1+1, index2+1);
    }

    public void swap(int index1, int index2, List<Message> group) {
        int x = index2+group.size()-1;
        int groupReplies = group.size()-1;
        for(int i = index2+group.size()-1; i != index2-1; i--) {
            swap(index1, x);
        }
    }



    public void resort(boolean resortReplies, int index) {
        int upvotes = messages.get(index).getUpvotes();
        if (resortReplies) {
            List<Message> remainingReplies = (List)messages.clone();
            remainingReplies = remainingReplies.subList(index +1, messages.get(index).getReplyCount() + index+1);

            for (int i = index + 1; i < (messages.get(index).getReplyCount() + index + 1); i++) {
                int biggestIndex = findBiggest(remainingReplies);
                Message biggest = remainingReplies.get(biggestIndex);
                remainingReplies.remove(biggestIndex);

                messages.set(i, biggest);
                messages.get(i).setIndex(i);
                myRef.child(messages.get(i).getID()).child("index").setValue(i);
            }
        }
        else {
            List<Message> group = new ArrayList<>();
            if(messages.get(index).getReplyCount() > 0) {
                group = (ArrayList)messages.clone();
                group = messages.subList(index, messages.get(index).getReplyCount() + index+1);
            }
            while(index!=0) {
                int pastIndex = index;
                //find closest parent uphill
                for(int i = index-1; i != -1; i--) {
                    //case that it is a parent
                    boolean isReply = messages.get(i).getReply();
                    if(!isReply ) {
                        index = i;
                        break;
                    }
                }

                if(messages.get(index).getUpvotes() < upvotes ) {
                    if(group.size() == 0)
                        swap(index, pastIndex);
                    else
                        swap(index, pastIndex, group);

                    int firstReply = index+messages.get(index).getReplyCount()+2;
                    int firstParent = firstReply + messages.get(firstReply-1).getReplyCount();
                    for(int i = firstReply; i != firstParent; i++ ) {
                        messages.get(i).setParentIndex(firstReply-1);
                        myRef.child(messages.get(i).getID()).child("parentIndex").setValue(firstReply-1);
                    }

                }
                else
                    break;
            }

        }
    }

    public void addMessage() {
        EditText message = (EditText) findViewById(R.id.editTextInput);

        if (!(message.getText().toString().equals(""))) {
            String key = myRef.child(myRef.push().getKey()).getKey();
            Message newMessage = new Message(message.getText().toString(), username, key, false, 0, -1, messages.size());
            myRef.child(key).setValue(newMessage);
            messages.add(newMessage);
            ((Adapter)adapter).update(messages, nameEntered);

            //Toast.makeText(this, "Successfully added message.", Toast.LENGTH_LONG).show();
            closeKeyboard();
        }

        message.setText("");
    }

    public void deleteMessage(int position) {
        String key = messages.get(position).getID();
        if(messages.get(position).getReply()) {
            messages.get(messages.get(position).getParentIndex()).decrementReplyCount();
            myRef.child(messages.get(messages.get(position).getParentIndex()).getID()).child("replyCount").setValue(messages.get(messages.get(position).getParentIndex()).getReplyCount());
            rippleIndexesDown(position+1, messages.get(position).getParentIndex());
        }
        else {
            String cKey;
            for (int i = 0; i < messages.get(position).getReplyCount(); i++) {
                cKey = messages.get(position + 1).getID();

                rippleIndexesDown(position+1, -1);
                messages.remove(position + 1);
                myRef.child(cKey).removeValue();
            }
        }

        if(!messages.get(position).getReply())
            rippleIndexesDown(position+1, -1);
        messages.remove(position);
        myRef.child(key).removeValue();

        ((Adapter)adapter).update(messages, nameEntered);
        //Toast.makeText(this, "Successfully deleted message.", Toast.LENGTH_LONG).show();
    }

    public void addReply(int position) {
        EditText message = (EditText) findViewById(R.id.editTextInput);
        int futureIndex = messages.get(position).getReplyCount() + position + 1;

        if (!(message.getText().toString().equals(""))) {
            String key = myRef.child(myRef.push().getKey()).getKey();
            Message newReply = new Message(message.getText().toString(), username, key, true, 0, position, futureIndex );

            rippleIndexesUp(futureIndex);

            messages.add(futureIndex, newReply);

            messages.get(position).incrementReplyCount();
            myRef.child( messages.get(position).getID()).child("replyCount").setValue(messages.get(position).getReplyCount());

            myRef.child(key).setValue(newReply);
            ((Adapter)adapter).update(messages, nameEntered);

            //Toast.makeText(this, "Successfully added message.", Toast.LENGTH_LONG).show();
            closeKeyboard();
            replying = false;
            message.setHint("Create a post!");
        }

        message.setText("");
    }

    public void upvote(int position) {
        messages.get(position).incrementUpvote();
        myRef.child(messages.get(position).getID()).child("upvotes").setValue(messages.get(position).getUpvotes());

        if(messages.get(position).getReply())
            resort(true, messages.get(position).getParentIndex());
        else
            resort(false, position);
        ((Adapter)adapter).update(messages, nameEntered);

    }

}
