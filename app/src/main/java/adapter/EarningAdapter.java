package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;

import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import model.Earning;
import model.Savings;

public class EarningAdapter extends RecyclerView.Adapter<EarningAdapter.ViewHolder> {

    private Context context;
    private List<Earning> data;
    private HashMap<String, String> user = new HashMap<>();

    public EarningAdapter(Context context, List<Earning> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Earning> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_earning_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        NumberFormat format = new DecimalFormat("#,###");

        holder.customer.setText(data.get(position).getCustomer());
        holder.source.setText(data.get(position).getSource());
        holder.type.setText(data.get(position).getType());
        holder.amount.setText("N" + format.format(Double.parseDouble(data.get(position).getAmount())));
        holder.earning.setText("N" + format.format(Double.parseDouble(data.get(position).getEarning())));
        holder.bonus.setText("N" + format.format(Double.parseDouble(data.get(position).getBonus())));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView customer, source, type, amount, earning, bonus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            customer = itemView.findViewById(R.id.customer);
            source = itemView.findViewById(R.id.source);
            type = itemView.findViewById(R.id.type);
            amount = itemView.findViewById(R.id.amount);
            earning = itemView.findViewById(R.id.earning);
            bonus = itemView.findViewById(R.id.bonus);
        }
    }
}