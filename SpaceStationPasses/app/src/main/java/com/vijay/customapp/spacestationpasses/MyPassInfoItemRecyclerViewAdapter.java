package com.vijay.customapp.spacestationpasses;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vijay.customapp.spacestationpasses.ISSPassInfo.ISSPassDetail;
import com.vijay.customapp.spacestationpasses.PassesInfoFragment.OnListFragmentInteractionListener;
import com.vijay.customapp.spacestationpasses.ISSPassInfo.ISSPassDetail.ISSPassesItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ISSPassesItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyPassInfoItemRecyclerViewAdapter extends RecyclerView.Adapter<MyPassInfoItemRecyclerViewAdapter.ViewHolder> {

    private List<ISSPassesItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyPassInfoItemRecyclerViewAdapter(List<ISSPassesItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    public void updateListItems(List<ISSPassesItem> lists) {
        mValues = lists;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_passinfoitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdRiseTimeView.setText(String.valueOf(mValues.get(position).risetimeInfo));
        holder.mDurationView.setText(String.valueOf(mValues.get(position).duration));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdRiseTimeView;
        public final TextView mDurationView;
        public ISSPassesItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdRiseTimeView = (TextView) view.findViewById(R.id.idrisetime);
            mDurationView = (TextView) view.findViewById(R.id.duration);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdRiseTimeView.getText() + " : " + mDurationView.getText() + "'";
        }
    }
}
