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
import model.Payout;

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> {

    private Context context;
    private List<Investment> data;

    public InterestAdapter(Context context, List<Investment> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Investment> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_interest_table, parent, false);
        return new InterestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat format = new DecimalFormat("#,###");
        String s = data.get(position).getInvestment();
        char firstLetter = s.charAt(0);

        holder.investment.setText(String.format("%s%s", Character.toString(firstLetter).toUpperCase(), s.substring(1)));
        holder.date.setText(data.get(position).getDay());
        holder.amount.setText(format.format(Double.parseDouble(data.get(position).getAmount())));
        holder.interest.setText(data.get(position).getInterest());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView investment, date, amount, interest;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            investment = itemView.findViewById(R.id.investment);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
            interest = itemView.findViewById(R.id.interest_accrued);
        }
    }
}
