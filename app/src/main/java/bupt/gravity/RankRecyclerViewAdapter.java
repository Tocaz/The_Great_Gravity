package bupt.gravity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RankRecyclerViewAdapter extends RecyclerView.Adapter<RankRecyclerViewAdapter.MyViewHolder>{

    private ArrayList<NameDistanceInfo> nameDistanceInfos;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView rank_name;
        TextView rank_distance;

        public MyViewHolder(View v) {
            super(v);
            rank_name = v.findViewById(R.id.rank_name);
            rank_distance = v.findViewById(R.id.rank_distance);
        }
    }


    public RankRecyclerViewAdapter(ArrayList<NameDistanceInfo> nameDistanceInfos) {
        this.nameDistanceInfos = nameDistanceInfos;
    }

    @Override
    public RankRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rank_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.rank_name.setText(nameDistanceInfos.get(position).getName());
        holder.rank_distance.setText(nameDistanceInfos.get(position).getDistance());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return nameDistanceInfos.size();
    }

}
