package com.example.ludoreddit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class Adapter extends RecyclerView.Adapter<Adapter.LudoViewHolder> {
    private ArrayList<Message> messages;
    private Context context;
    private boolean nameEntered;

    private OnMessageClickListener mListener;

    public interface OnMessageClickListener {

        void onDeleteClick(int position);

        void onReplyClick(int position);

        void onUpvoteClick(int position);

    }
    public void setOnMessageClickListener(OnMessageClickListener listener) {
        mListener = listener;
    }

    public Adapter(Context context) {
        messages = new ArrayList<Message>();
        this.context = context;
    }

    public void update(ArrayList<Message> newMessages, boolean nameEntered) {
        messages.clear();
        messages.addAll(newMessages);
        this.nameEntered = nameEntered;

        notifyDataSetChanged();
    }


    public class LudoViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageButton delete;
        ImageButton upvote;
        ImageButton reply;
        FloatingActionButton send;

        float messageX;
        float upvoteX;
        float deleteX;


        public LudoViewHolder(View view) {
            super(view);

            message = (TextView) view.findViewById(R.id.textViewMessage);
            delete = (ImageButton) view.findViewById(R.id.btnDelete);
            upvote = (ImageButton) view.findViewById(R.id.btnUpvote);
            reply = (ImageButton) view.findViewById(R.id.btnReply);
            send = (FloatingActionButton) view.findViewById(R.id.floatingActionBtnSend);

            messageX = message.getX();
            upvoteX = upvote.getX();
            deleteX = delete.getX();


            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteClick(position);
                        }
                    }
                }

            });

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {


                            mListener.onReplyClick(position);
                        }
                    }
                }

            });

            upvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onUpvoteClick(position);
                        }
                    }
                }

            });

        }
    }

            @NonNull
    @Override
    public LudoViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view, parent, false);
        LudoViewHolder viewHolder = new LudoViewHolder(v);
        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull LudoViewHolder holder, int position) {
        if(nameEntered) {
            ((LudoViewHolder)holder).upvote.setEnabled(true);
            ((LudoViewHolder)holder).delete.setEnabled(true);
            ((LudoViewHolder)holder).reply.setEnabled(true);
        }
        else {
            ((LudoViewHolder)holder).upvote.setEnabled(false);
            ((LudoViewHolder)holder).delete.setEnabled(false);
            ((LudoViewHolder)holder).reply.setEnabled(false);
        }
        Message currM = messages.get(position);
        ((LudoViewHolder)holder).message.setMovementMethod(new ScrollingMovementMethod());
        ((LudoViewHolder)holder).message.setPadding(10,0,0,0);
        ((LudoViewHolder)holder).reply.setVisibility(ImageView.VISIBLE);
        ((LudoViewHolder)holder).message.setX(0);
        String num = Integer.toString(currM.getUpvotes());
        SpannableStringBuilder str = new SpannableStringBuilder(num + "↑ " +  currM.getName() + ": " + currM.getContents());
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, num.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan red= new ForegroundColorSpan(Color.rgb(0,176,6));
        str.setSpan(red, 0, num.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if(currM.getReply()) {
            ((LudoViewHolder)holder).reply.setVisibility(ImageView.GONE);
            ((LudoViewHolder)holder).message.setPadding(75, 0 , 0,0);

        }
        ((LudoViewHolder)holder).message.setText(str);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}

