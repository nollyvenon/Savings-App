package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.snowcorp.login.R;

import java.util.List;

import model.Savings;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    private Context context;
    private List<Savings> data;

    public SavingsAdapter(Context context, List<Savings> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Savings> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_savings_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String s = data.get(position).getSavings();
        Character firstLetter = s.charAt(0);
        holder.savingT.setText(R.string.savings);
        holder.savingV.setText(firstLetter.toString().toUpperCase() + s.substring(1, s.length()));

        holder.amountT.setText(R.string.amount);
        holder.amountV.setText(data.get(position).getBalance());

        holder.startDateT.setText(R.string.start_date);
        holder.startDateV.setText(data.get(position).getDate_start());

        holder.endDateT.setText(R.string.end_date);
        holder.endDateV.setText(data.get(position).getDate_end());

        holder.dailyAmountT.setText(R.string.daily_amount);
        holder.dailyAmountV.setText(data.get(position).getAmount());

        holder.statusT.setText(R.string.status);
        holder.statusV.setText(data.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView savingT;
        TextView savingV;
        TextView amountT;
        TextView amountV;
        TextView startDateT;
        TextView startDateV;
        TextView endDateT;
        TextView endDateV;
        TextView dailyAmountT;
        TextView dailyAmountV;
        TextView statusT;
        TextView statusV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            savingT = itemView.findViewById(R.id.header_title_saving);
            savingV = itemView.findViewById(R.id.header_value_saving);
            amountT = itemView.findViewById(R.id.header_title_amount);
            amountV = itemView.findViewById(R.id.header_value_amount);
            startDateT = itemView.findViewById(R.id.header_title_start_date);
            startDateV = itemView.findViewById(R.id.header_value_start_date);
            endDateT = itemView.findViewById(R.id.header_title_end_date);
            endDateV = itemView.findViewById(R.id.header_value_end_date);
            dailyAmountT = itemView.findViewById(R.id.header_title_daily_amount);
            dailyAmountV = itemView.findViewById(R.id.header_value_daily_amount);
            statusT = itemView.findViewById(R.id.header_title_status);
            statusV = itemView.findViewById(R.id.header_value_status);
        }
    }
}