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

import model.Payout;

public class PayoutAdapter extends RecyclerView.Adapter<PayoutAdapter.ViewHolder> {

    private Context context;
    private List<Payout> data;

    public PayoutAdapter(Context context, List<Payout> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Payout> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PayoutAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_payout_table, parent, false);
        return new PayoutAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat format = new DecimalFormat("#,###");
        String s = data.get(position).getInvestment();
        if(s.equalsIgnoreCase("Null")) {
            s = "Savings Withdrawal";
        }
        char firstLetter = s.charAt(0);

        holder.transaction.setText(String.format("%s%s", Character.toString(firstLetter).toUpperCase(), s.substring(1)));
        holder.date.setText(data.get(position).getDate_requested());
        holder.amount.setText(format.format(Double.parseDouble(data.get(position).getAmount())));
        holder.status.setText(data.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView transaction, date, amount, status;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            transaction = itemView.findViewById(R.id.transaction);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
            status = itemView.findViewById(R.id.status);
        }
    }
}
