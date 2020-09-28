package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import model.Investment;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder> {

    private Context context;
    private List<Investment> data;
    private HashMap<String, String> user = new HashMap<>();

    public InvestmentAdapter(Context context, List<Investment> data) {
        this.context = context;
        this.data = data;
    }

    public void setData(List<Investment> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvestmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_investment_table, parent, false);
        return new InvestmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat format = new DecimalFormat("#,###");
        String s = data.get(position).getInvestment();
        char firstLetter = s.charAt(0);
        holder.investmentT.setText(R.string.investments);
        holder.investmentV.setText(String.format("%s%s", Character.toString(firstLetter).toUpperCase(), s.substring(1)));

        holder.amountT.setText(R.string.amount);
        holder.amountV.setText(format.format(Double.parseDouble(data.get(position).getAmount())));

        holder.dayT.setText(R.string.day);
        holder.dayV.setText(data.get(position).getDay());

        holder.totalT.setText(R.string.interest_accrued);
        holder.totalV.setText(format.format(Double.parseDouble(data.get(position).getInterest())));

        holder.interestT.setText(R.string.total);
        holder.interestV.setText(format.format(Double.parseDouble(data.get(position).getTotal())));

        holder.startDateT.setText(R.string.start_date);
        holder.startDateV.setText(data.get(position).getStart());

        String st = data.get(position).getStatus();
        char sfirstLetter = st.charAt(0);
        holder.statusT.setText(R.string.status);
        holder.statusV.setText(String.format("%s%s", Character.toString(sfirstLetter).toUpperCase(), st.substring(1)));

        if(data.get(position).getStatus().equalsIgnoreCase("withdrawn")) {
            holder.withdraw.setVisibility(View.GONE);
        }

        holder.withdraw.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirm withdrawal?");
            builder.setPositiveButton("Yes", (dialog, id) -> {
                DatabaseHandler db = new DatabaseHandler(context);
                user = db.getUserDetails();
                Functions.InvestmentWithdraw(context, user.get("uid"), String.valueOf(data.get(position).getId()));
            })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView investmentT, investmentV, amountT, amountV, dayT, dayV, interestT;
        TextView interestV, totalT, totalV, startDateT, startDateV, statusT, statusV;
        Button withdraw;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            investmentT = itemView.findViewById(R.id.header_title_investment);
            investmentV = itemView.findViewById(R.id.header_value_investment);
            amountT = itemView.findViewById(R.id.header_title_amount);
            amountV = itemView.findViewById(R.id.header_value_amount);
            totalT = itemView.findViewById(R.id.header_title_total);
            totalV = itemView.findViewById(R.id.header_value_total);
            dayT = itemView.findViewById(R.id.header_title_day);
            dayV = itemView.findViewById(R.id.header_value_day);
            interestT = itemView.findViewById(R.id.header_title_interest);
            interestV = itemView.findViewById(R.id.header_value_interest);
            startDateT = itemView.findViewById(R.id.header_title_start_date);
            startDateV = itemView.findViewById(R.id.header_value_start_date);
            statusT = itemView.findViewById(R.id.header_title_status);
            statusV = itemView.findViewById(R.id.header_value_status);
            withdraw = itemView.findViewById(R.id.withdraw);
        }
    }
}
