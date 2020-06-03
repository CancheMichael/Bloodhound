/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.dartmouth.cs65.searchandrescue.R;

public class ChatMessageFRAdapter extends FirestoreRecyclerAdapter<ChatMessage, ChatMessageViewHolder> {

    public ChatMessageFRAdapter(@NonNull Context context, Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<ChatMessage>().setQuery(query, ChatMessage.class)
                .build());
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position, @NonNull ChatMessage model) {
        //Link TextViews with message text
        final TextView mText = holder.mText;
        final TextView mName = holder.mName;
        final TextView mTime = holder.mTime;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy (HH:mm a)");

        mName.setText(model.getUser());
        mText.setText(model.getText());
        mTime.setText(sdf.format(Calendar.getInstance().getTime()));

        }


    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //Setup layout for message bubbles
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messagein, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return 2;
    }

}
