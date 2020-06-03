/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.structures;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.dartmouth.cs65.searchandrescue.R;

//Stores the TextViews for the messages for the ChatActivity
public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    TextView mText;
    TextView mName;
    TextView mTime;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        //Setup TextViews with id
        mText = itemView.findViewById(R.id.messView);
        mName = itemView.findViewById(R.id.nameView);
        mTime = itemView.findViewById(R.id.timeView);
    }
}