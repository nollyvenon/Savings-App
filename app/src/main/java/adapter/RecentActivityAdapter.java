package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import model.Investment;
import model.RecentActivity;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private Context context;
    private List<RecentActivity> data;

    public RecentActivityAdapter(Context context, List<RecentActivity> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<RecentActivity> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_recent_activity_table, parent, false);
        return new RecentActivityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat format = new DecimalFormat("#,###");
        String date = data.get(position).getDate();

        holder.date.setText(date);
        holder.transaction.setText(format.format(Double.parseDouble(data.get(position).getAmount())) + " " + data.get(position).getDescription());
        holder.customer.setText(data.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, customer, transaction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            date.setSingleLine(false);
            customer = itemView.findViewById(R.id.customer);
            transaction = itemView.findViewById(R.id.transaction);
            transaction.setSingleLine(false);
        }
    }
}
